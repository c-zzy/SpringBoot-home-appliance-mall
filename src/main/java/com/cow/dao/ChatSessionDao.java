package com.cow.dao;

import com.cow.entity.ChatSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

public interface ChatSessionDao {

    // 【關鍵修復】：給所有帶底線的欄位加上 AS 別名，否則前端抓不到 user_name 和 avatar_url，會導致所有人都長得一模一樣！
    @Select("SELECT s.id, s.user_id AS userId, s.last_msg AS lastMsg, s.last_msg_time AS lastMsgTime, " +
            "s.status, s.create_time AS createTime, u.user_name AS userName, u.avatar_url AS avatarUrl " +
            "FROM chat_session s " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "ORDER BY s.last_msg_time DESC")
    List<ChatSession> selectAll();

    @Update("UPDATE chat_session SET status = #{status}, last_msg = #{lastMsg}, last_msg_time = #{lastMsgTime} WHERE id = #{id}")
    int updateById(ChatSession chatSession);

    // LONG 類型
    @Select("SELECT id FROM chat_session WHERE user_id = #{userId}")
    Long getSessionIdByUserId(Long userId);

    @Insert("INSERT INTO chat_session(user_id, last_msg, last_msg_time, status, create_time) " +
            "VALUES(#{userId}, #{lastMsg}, #{lastMsgTime}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ChatSession chatSession);
}