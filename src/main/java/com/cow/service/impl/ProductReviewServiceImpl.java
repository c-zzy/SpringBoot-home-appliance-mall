package com.cow.service.impl;

import com.cow.dao.ProductReviewDao;
import com.cow.entity.ProductReview;
import com.cow.service.ProductReviewService;
import com.cow.util.general.WordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 *
 * @email QQ550080747
 * @date 2021/1/4 11:09
 * @description 商品评价业务逻辑
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class ProductReviewServiceImpl implements ProductReviewService {
    @Autowired
    private ProductReviewDao productReviewDao;

    @Override
    public ProductReview selectById(Integer reviewId) {
        return productReviewDao.selectById(reviewId);
    }

    @Override
    public List<Map<String, Object>> selectAll(String productNo) {
        return productReviewDao.selectAll(productNo);
    }

    @Override
    public Boolean insertData(ProductReview productReview) {
        productReview.setReviewTime(new Date());
        String review = WordFilter.replaceWords(productReview.getProductReview());
        productReview.setProductReview(review);
        return productReviewDao.insertData(productReview);
    }

    @Override
    public Boolean updateById(ProductReview productReview) {
        return productReviewDao.updateById(productReview);
    }

    @Override
    public Boolean deleteById(Integer reviewId) {
        return productReviewDao.deleteById(reviewId);
    }

    @Override
    public Map<String, Map<String, Double>> getUserItemMatrix() {
        // 获取用户评价列表
        List<ProductReview> productReviewList = productReviewDao.selectAllList();

        // 获取用户评价矩阵
        Map<String, Map<String, Double>> matrix = new HashMap<>();

        for (ProductReview productReview : productReviewList) {
            matrix.computeIfAbsent(productReview.getAccountNumber(), k -> new HashMap<>())
                    .put(productReview.getProductNo(), productReview.getStarLevel());
        }

        return matrix;
    }

    @Override
    public int selectCount() {
        return productReviewDao.selectCount();
    }

    @Override
    public List<ProductReview> selectAllList() {
        return productReviewDao.selectAllList();
    }

    // ====================== 根据订单号查询评价 ======================
    @Override
    public ProductReview getByOrderNo(String orderNo) {
        return productReviewDao.selectByOrderNo(orderNo);
    }

    // ====================== 根据订单号删除评价（给你加好了） ======================
    @Override
    public boolean deleteByOrderNo(String orderNo) {
        ProductReview review = productReviewDao.selectByOrderNo(orderNo);
        if (review == null) {
            return false;
        }
        return productReviewDao.deleteById(review.getReviewId());
    }
}