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
        // 频控：如果是系统/AI(senderType=0)发出的“转人工”提示，进行防重复拦截
        if (senderType != null && senderType == 0 && content != null && content.contains("将转人工客服")) {
            String redisLockKey = "lock:ai_kefu:transfer:session:" + sessionId;
            synchronized (String.valueOf(sessionId).intern()) {
                if (Boolean.TRUE.equals(redisTemplate.hasKey(redisLockKey))) {
                    System.out.println("【底层物理拦截】会话 " + sessionId + " 已存在转人工锁，拒绝重写入库。");
                    return; // 直接丢弃，不再入库，前端轮询也查不到数据，死循环瓦解
                }
                redisTemplate.opsForValue().set(redisLockKey, "locked", 30, TimeUnit.MINUTES);
            }
        }

        // 正常入库逻辑
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