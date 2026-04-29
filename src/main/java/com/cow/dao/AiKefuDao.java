package com.cow.dao;

import com.cow.entity.AiKefu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @email QQ550080747
 * @date 2025/04/28
 * @description 智能客服问答
 */
public interface AiKefuDao extends BaseDao<AiKefu> {

    /**
     * 查询所有智能客服问题
     *
     * @return 问题列表
     */
    List<String> selectAllQuestion();

    /**
     * 查询问题是否存在
     *
     * @param aiId       编号
     * @param aiQuestion 问题
     * @return 是否存在
     */
    Boolean existsWithQuestionName(@Param("aiId") Integer aiId, @Param("aiQuestion") String aiQuestion);

}