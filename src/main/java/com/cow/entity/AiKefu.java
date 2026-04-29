package com.cow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @email QQ550080747
 * @date 2025/04/28
 * @description 智能客服问答表
 */
@Data
@Entity
@Table(name = "ai_kefu")
@NoArgsConstructor
@AllArgsConstructor
public class AiKefu {

    @Id
    private Integer aiId;          // 主键 ai_id
    private String aiQuestion;     // 问题 ai_question
    private String aiAnswer;       // 回答 ai_answer
    private Boolean status;        // 状态 1启用 0禁用
}