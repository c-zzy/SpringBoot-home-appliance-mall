package com.cow.dao;

import com.cow.entity.ChatMessage;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import java.util.List;

public interface ChatMessageDao {

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> selectBySessionId(Long sessionId);

    @Insert("INSERT INTO chat_message(session_id, sender_type, content, create_time) " +
            "VALUES(#{sessionId}, #{senderType}, #{content}, #{createTime})")
    int insert(ChatMessage message);
}