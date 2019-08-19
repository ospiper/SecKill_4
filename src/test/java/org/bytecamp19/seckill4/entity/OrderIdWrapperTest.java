package org.bytecamp19.seckill4.entity;

import org.bytecamp19.seckill4.service.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by LLAP on 2019/8/19.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderIdWrapperTest {
    @Autowired
    private OrderService orderService;

    @Test
    public void testOrderIdGeneration() {
        int pid = 233;
        int uid = 817;
        int price = 1926;
        String orderId = orderService.generateOrderId(pid, uid, price);
//        System.out.println(orderId);
        OrderIdWrapper id = orderService.validateOrderId(orderId, uid, price);
        assertEquals(pid, id.getPid());
        assertEquals(uid, id.getUid());
        assertEquals(price, id.getPrice());
        assertNull(orderService.validateOrderId(orderId.substring(orderId.length() / 2), uid, price));
        assertNull(orderService.validateOrderId(orderId, uid, 0));
        assertEquals(price, id.getPrice());
    }
}
