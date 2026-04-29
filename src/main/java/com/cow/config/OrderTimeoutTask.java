package com.cow.config;

import com.cow.dao.OrderDao;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutTask {

    private final OrderDao orderDao;

    public OrderTimeoutTask(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    // 每 1 分钟执行一次
    @Scheduled(fixedRate = 60000)
    public void closeTimeoutOrder() {
        orderDao.autoCloseTimeoutOrder();
    }
}