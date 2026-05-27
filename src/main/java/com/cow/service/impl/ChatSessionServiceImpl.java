package com.cow.service.impl;

import com.cow.dao.ChatMessageDao;
import com.cow.dao.ChatSessionDao;
import com.cow.entity.ChatMessage;
import com.cow.entity.ChatSession;
import com.cow.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate; // 導入 Redis 模板
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit; // 導入 TimeUnit

@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionDao chatSessionDao;

    @Autowired
    private ChatMessageDao chatMessageDao;

    @Autowired
    private StringRedisTemplate redisTemplate; // 注入純文本 Redis 模板，確保 hasKey 完美匹配

    @Override
    public List<ChatSession> getSessionList() {
        return chatSessionDao.selectAll();
    }

    @Override
    public List<ChatMessage> getMessageList(Long sessionId) {
        return chatMessageDao.selectBySessionId(sessionId);
    }

    @Override
    public void bindAdmin(Long sessionId) {
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setStatus(2);
        chatSessionDao.updateById(session);
    }

    @Override
    public void sendMessage(Long sessionId, Integer senderType, String content) {

        // 【核心物理阻斷防線】：如果消息是系統或 AI 發出的（senderType == 0），並且內容中包含「转人工」核心詞
        // 使用 content.contains() 模糊匹配，可以完美杜絕因前端傳輸帶有看不見的換行符或空格導致 equals() 失效的問題
        if (senderType != null && senderType == 0 && content != null && content.contains("将转人工客服")) {
            String redisLockKey = "lock:ai_kefu:transfer:session:" + sessionId;

            // 使用 JVM intern 鎖對當前會話強行互斥排隊
            synchronized (String.valueOf(sessionId).intern()) {
                Boolean hasLock = redisTemplate.hasKey(redisLockKey);
                if (hasLock != null && hasLock) {
                    // 半小時內發過一次，底層直接攔截拋棄，既不寫數據庫，也不會被前端輪詢抓到，死循環瞬間瓦解！
                    System.out.println("【核心底層絕殺】會話 " + sessionId + " 觸發死循環安全機制，本次拒絕寫入數據庫！");
                    return;
                }
                // 半小時內第一次觸發，立刻搶先把 Redis 鎖焊死，過期時間設置為 30 分鐘
                redisTemplate.opsForValue().set(redisLockKey, "locked", 30, TimeUnit.MINUTES);
            }
        }

        // ================= 下方為你原本完好無損的物理入庫邏輯 =================
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType(senderType);
        msg.setContent(content);
        msg.setCreateTime(new Date());
        chatMessageDao.insert(msg);

        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setLastMsg(content);
        session.setLastMsgTime(new Date());
        chatSessionDao.updateById(session);
    }

    // ===================== 全部改成 LONG =====================
    @Override
    public Long getOrCreateSession(Long userId) {
        if (userId == null) {
            throw new RuntimeException("userId 不能为空");
        }
        Long sessionId = chatSessionDao.getSessionIdByUserId(userId);
        if (sessionId != null) {
            return sessionId;
        }
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setLastMsg("欢迎咨询客服");
        session.setLastMsgTime(new Date());
        session.setStatus(1);
        session.setCreateTime(new Date());
        chatSessionDao.insert(session);
        return session.getId();
    }
}