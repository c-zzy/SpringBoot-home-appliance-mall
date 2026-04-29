package com.cow.controller;

import com.cow.entity.Order;
import com.cow.entity.ProductReview;
import com.cow.service.OrderService;
import com.cow.service.ProductReviewService;
import com.cow.util.general.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 *
 * @email QQ550080747
 * @description 商品评价 业务类
 */
@RestController
@CrossOrigin
public class ProductReviewController {
    private static final String EVALUATION_STATE = "已评价";

    @Autowired
    private ProductReviewService productReviewService;

    @Autowired
    private OrderService orderService;

    /**
     * 添加商品评论
     *
     * @param productReview 商品评论
     */
    @RequestMapping(value = "/productReview/add")
    public CommonResult addProductReview(ProductReview productReview) {
        if (productReviewService.insertData(productReview)) {
            Integer orderId = orderService.selectIdByKey(productReview.getOrderNo());
            Order order = new Order();
            order.setOrderId(orderId);
            order.setOrderState(EVALUATION_STATE);
            orderService.updateById(order);
            return CommonResult.success("商品评论添加成功", productReview);
        }
        return CommonResult.error("商品评论添加失败");
    }

    @RequestMapping(value = "/productReview/update")
    public CommonResult updateProductReview(ProductReview productReview) {
        if (productReviewService.updateById(productReview)) {
            return CommonResult.success("商品评论修改成功", productReview);
        }
        return CommonResult.error("商品评论修改失败");
    }

    @RequestMapping(value = "/productReview/deleteById")
    public CommonResult deleteProductReview(Integer reviewId) {
        if (productReviewService.deleteById(reviewId)) {
            return CommonResult.success("商品评论删除成功", "reviewId: " + reviewId);
        }
        return CommonResult.error("商品评论删除失败");
    }

    // ====================== 【你要的：根据订单号删除评价】 ======================
    @RequestMapping(value = "/productReview/delete")
    public CommonResult deleteByOrderNo(@RequestParam String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return CommonResult.error("订单号不能为空");
        }

        // 1. 先根据订单号查到评价
        ProductReview review = productReviewService.getByOrderNo(orderNo);
        if (review == null) {
            return CommonResult.error("该订单暂无评价，无法删除");
        }

        // 2. 根据评价ID删除
        boolean deleteSuccess = productReviewService.deleteById(review.getReviewId());
        if (deleteSuccess) {
            return CommonResult.success("评价删除成功");
        } else {
            return CommonResult.error("评价删除失败");
        }
    }


    @RequestMapping(value = "/productReview/findAll")
    public CommonResult findAllProductReview(String productNo) {
        List<Map<String, Object>> productReviewInfo = productReviewService.selectAll(productNo);
        if (productReviewInfo != null) {
            return CommonResult.success("商品评论查询成功", productReviewInfo);
        }
        return CommonResult.error("商品评论查询失败");
    }

    @RequestMapping(value = "/productReview/findById")
    public CommonResult findById(Integer reviewId) {
        ProductReview productReview = productReviewService.selectById(reviewId);
        if (productReview != null) {
            return CommonResult.success("商品评论查询成功", productReview);
        }
        return CommonResult.error("商品评论查询失败");
    }

    @RequestMapping(value = "/productReview/findCount")
    public CommonResult findCount() {
        int count = productReviewService.selectCount();
        return CommonResult.success("商品评论数量查询成功", count);
    }

    @RequestMapping(value = "/productReview/list")
    public CommonResult findAllProductReview() {
        List<ProductReview> productReviewInfo = productReviewService.selectAllList();
        if (productReviewInfo != null) {
            return CommonResult.success("商品评论查询成功", productReviewInfo);
        }
        return CommonResult.error("商品评论查询失败");
    }

    // ====================== 【新增】根据订单号查询评价 ======================
    @RequestMapping(value = "/productReview/getByOrderNo")
    public CommonResult getByOrderNo(@RequestParam String orderNo) {
        ProductReview review = productReviewService.getByOrderNo(orderNo);
        if (review != null) {
            return CommonResult.success("查询评价成功", review);
        }
        return CommonResult.error("暂无评价");
    }

}