package com.cow.service.impl;

import com.cow.dao.ChatMessageDao;
import com.cow.dao.ChatSessionDao;
import com.cow.entity.ChatMessage;
import com.cow.entity.ChatSession;
import com.cow.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionDao chatSessionDao;

    @Autowired
    private ChatMessageDao chatMessageDao;

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