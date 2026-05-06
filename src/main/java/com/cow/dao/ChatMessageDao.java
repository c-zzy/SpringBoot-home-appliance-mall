package com.cow.dao;

import com.cow.entity.ChatMessage;
import org.apache.ibatis.annotations.Param; // 記得導入這個
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import java.util.List;

public interface ChatMessageDao {

    // 【關鍵修復】：加上 @Param("sessionId")，防止 MyBatis 找不到參數導致查詢錯亂
    @Select("SELECT id, session_id AS sessionId, sender_type AS senderType, content, create_time AS createTime " +
            "FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    @Insert("INSERT INTO chat_message(session_id, sender_type, content, create_time) " +
            "VALUES(#{sessionId}, #{senderType}, #{content}, #{createTime})")
    int insert(ChatMessage message);
}