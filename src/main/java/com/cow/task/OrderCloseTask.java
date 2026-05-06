package com.cow.task;

import com.cow.dao.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderCloseTask {

    @Autowired
    private OrderDao orderDao;

    // 每小时执行一次（整点），你也可以改成每天凌晨执行，比如 0 0 2 * * ?
    @Scheduled(cron = "0 0 * * * ?")
    public void closeTimeoutOrders() {
        orderDao.autoCloseTimeoutOrder();
        System.out.println("【定时任务】已执行超时订单自动关闭");
    }
}