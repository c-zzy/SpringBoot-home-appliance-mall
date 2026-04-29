package com.cow.entity;

import lombok.Data;
import java.util.Date;

@Data
public class ChatMessage {
    private Long id;          // 主键
    private Long sessionId;    // 会话ID
    private Integer senderType;// 1=用户 2=客服
    private String content;    // 消息内容
    private Date createTime;   // 创建时间
}