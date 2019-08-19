package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.cache.InventoryManager;
import org.bytecamp19.seckill4.cache.OrderLimitManager;
import org.bytecamp19.seckill4.cache.OrderMessage;
import org.bytecamp19.seckill4.controller.MainController;
import org.bytecamp19.seckill4.entity.OrderIdWrapper;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.mapper.ProductMapper;
import org.bytecamp19.seckill4.service.OrderService;
import org.junit.After;
import org.junit.Before;
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
    private OrderService orderService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryManager inventoryManager;
    @Autowired
    private OrderLimitManager orderLimitManager;

    private final int pid = 177620431;
    private final int uid = 175230;

    @Before
    public void before() {
        orderLimitManager.clearLimits();
        inventoryManager.clearInventory();
    }

    @Test
    public void placeIllegallyOrderTest() {
        Product product = productMapper.selectById(pid);
        System.out.println(product);
        try {
            OrderMessage message = orderService.placeOrder(uid, product);
            fail();
            System.out.println(message);
        }
        catch (ForbiddenException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void placeOrderTest() {
        Product product = productService.getProduct(pid);
        System.out.println(product);
        try {
            OrderMessage message = orderService.placeOrder(uid, product);
            OrderMessage m2 = orderService.placeOrder(uid, product);
            assertNull(m2);
            System.out.println(message);
        }
        catch (ForbiddenException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @After
    public void after() {
        orderLimitManager.clearLimits();
        inventoryManager.clearInventory();
    }
}
