package com.cow.dao;

import com.cow.entity.ProductReview;

import java.util.List;
import java.util.Map;

/**
 *
 * @email QQ550080747
 * @date 2021/1/4 10:52
 * @description 商品评价
 */
public interface ProductReviewDao extends BaseDao<ProductReview> {

    /**
     * 查询所有
     *
     * @param productNo 商品编号
     * @return 商品评论信息
     */
    List<Map<String, Object>> selectAll(String productNo);

    /**
     * 查询所有评论信息
     *
     * @return 评论信息
     */
    List<ProductReview> selectAllList();

    /**
     * 根据订单号查询评价
     */
    ProductReview selectByOrderNo(String orderNo);

}