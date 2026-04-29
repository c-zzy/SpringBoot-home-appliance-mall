package com.cow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    private Long id;           // 会话主键
    private Long userId;       // 用户ID（和user表user_id关联）
    private String userName;   // 用户昵称（来自user表）
    private String lastMsg;    // 最后一条消息
    private Date lastMsgTime;  // 最后消息时间
    private Integer status;     // 会话状态
    private Date createTime;    // 创建时间
    private String avatarUrl;   // 用户头像（来自user表）
    // 生成 getter 和 setter
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}