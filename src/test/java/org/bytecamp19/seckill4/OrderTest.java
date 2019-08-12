package org.bytecamp19.seckill4;

import lombok.Data;
import lombok.Getter;
import org.bytecamp19.seckill4.controller.MainController;
import org.bytecamp19.seckill4.service.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderTest {
    @Autowired
    private MainController mainController;
    @Autowired
    private OrderService orderService;
    private static boolean debug = true;

    @Data
    @Getter
    private static class OrderId {
        private final String orderId;
        private final long timestamp;
        private final int uid;
        private final int pid;
        private final int rand;
        private final int check;
        public OrderId(String orderId) {
            this.orderId = orderId;
            String[] segments = orderId.split("\\.");
            timestamp = Long.parseLong(segments[0]);
            uid = Integer.parseInt(segments[1]);
            pid = Integer.parseInt(segments[2]);
            rand = Integer.parseInt(segments[3]);
            check = Integer.parseInt(segments[4]);
        }
    }

    @Test
    public void testOrderIdGeneration() {
        int pid = 233;
        int uid = 817;
        int price = 1926;
        String orderId = orderService.generateOrderId(pid, uid, price);
//        System.out.println(orderId);
        assertEquals(pid, orderService.validateOrderId(orderId, uid, price));
        assertEquals(-1, orderService.validateOrderId(orderId.substring(orderId.length() / 2), uid, price));
        assertEquals(-3, orderService.validateOrderId(orderId, uid, 0));
    }
}
