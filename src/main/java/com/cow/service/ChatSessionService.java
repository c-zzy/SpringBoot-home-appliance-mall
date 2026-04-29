package com.cow.service;

import com.cow.entity.ChatMessage;
import com.cow.entity.ChatSession;
import java.util.List;

public interface ChatSessionService {
    List<ChatSession> getSessionList();
    List<ChatMessage> getMessageList(Long sessionId);
    void bindAdmin(Long sessionId);
    void sendMessage(Long sessionId, Integer senderType, String content);

    // 统一改成 LONG！！！
    Long getOrCreateSession(Long userId);
}