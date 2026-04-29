package com.cow.service;

import com.cow.entity.AiKefu;

import java.util.List;

/**
 * @email QQ550080747
 * @date 2025/04/28
 * @description 智能客服业务逻辑
 */
public interface AiKefuService {
    /**
     * 根据ID查询
     *
     * @param aiId 客服ID
     * @return 客服信息
     */
    AiKefu selectById(Integer aiId);

    /**
     * 查询所有客服问答
     *
     * @return 问答列表
     */
    List<AiKefu> selectAll();

    /**
     * 查询所有客服问题
     *
     * @return 问题列表
     */
    List<String> selectAllQuestion();

    /**
     * 查询问题是否存在
     *
     * @param aiId       ID
     * @param aiQuestion 问题
     * @return 是否存在
     */
    Boolean existsWithQuestionName(Integer aiId, String aiQuestion);

    /**
     * 新增客服问答
     *
     * @param aiKefu 问答
     * @return 是否新增成功
     */
    Boolean insertData(AiKefu aiKefu);

    /**
     * 更新客服问答
     *
     * @param aiKefu 问答
     * @return 是否更新成功
     */
    Boolean updateById(AiKefu aiKefu);

    /**
     * 删除客服问答
     *
     * @param aiId ID
     * @return 是否删除成功
     */
    Boolean deleteById(Integer aiId);
}