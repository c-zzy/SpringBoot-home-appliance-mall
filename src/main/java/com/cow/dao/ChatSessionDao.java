package com.cow.dao;

import com.cow.entity.ChatSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

public interface ChatSessionDao {

    @Select("SELECT s.*, u.user_name, u.avatar_url " +
            "FROM chat_session s " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "ORDER BY s.last_msg_time DESC")
    List<ChatSession> selectAll();

    @Update("UPDATE chat_session SET status = #{status}, last_msg = #{lastMsg}, last_msg_time = #{lastMsgTime} WHERE id = #{id}")
    int updateById(ChatSession chatSession);

    // LONG 类型
    @Select("SELECT id FROM chat_session WHERE user_id = #{userId}")
    Long getSessionIdByUserId(Long userId);

    @Insert("INSERT INTO chat_session(user_id, last_msg, last_msg_time, status, create_time) " +
            "VALUES(#{userId}, #{lastMsg}, #{lastMsgTime}, #{status}, #{createTime})")
    int insert(ChatSession chatSession);
}