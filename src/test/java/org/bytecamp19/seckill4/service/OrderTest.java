package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.cache.InventoryManager;
import org.bytecamp19.seckill4.cache.OrderLimitManager;
import org.bytecamp19.seckill4.cache.OrderMessage;
import org.bytecamp19.seckill4.cache.RedisMessageQueue;
import org.bytecamp19.seckill4.controller.MainController;
import org.bytecamp19.seckill4.entity.Order;
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
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @Autowired
    private RedisMessageQueue mq;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private ListOperations<String, String> listOperations;

    private static final String queueName = "orderQueue";
    private static final String payHashName = "paidOrder";

    private final long pid = 177620431;
    private final long uid = 175230;
    private final int price = 100;
    @Before
    public void before() {
        orderLimitManager.clearLimits();
        inventoryManager.clearInventory();
        mq.clear();
        listOperations = stringRedisTemplate.opsForList();
    }

    @Test
    public void placeIllegalOrderTest() {
//        Product product = productMapper.selectById(pid);
//        System.out.println(product);
//        try {
//            OrderMessage message = orderService.placeOrder(uid, product);
//            fail();
//            System.out.println(message);
//        }
//        catch (ForbiddenException ex) {
//            assertNotNull(ex);
//        }
    }

    @Test
    public void placeOrderTest() throws ForbiddenException {
        Product product = productService.getProduct(pid);
        System.out.println(product);
        try {
            Order message = orderService.placeOrder(uid, product);
            Order m2 = orderService.placeOrder(uid, product);
            assertNull(m2);
            System.out.println(message);
        }
        catch (ForbiddenException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void requestTokenTest() {
        final String orderId = "1566405592619.175230.177620431.173.101";
        OrderIdWrapper wrapper = new OrderIdWrapper(orderId);
        orderService.getToken(wrapper);
    }

    /** Run this test WITHOUT consumer running!! */
    @Test
    public void unWrittenPayTest() throws ForbiddenException {
        Product product = productService.getProduct(pid);
        try {
            Order message = orderService.placeOrder(uid, product);
            System.out.println(message);
            OrderIdWrapper wrapper = new OrderIdWrapper(message.getOrderId());
            Order o = orderService.payOrder(wrapper);
            System.out.println(o);
        }
        catch (ForbiddenException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void payTestAllBranches() {
        // TODO: There are 3 different scenarios in payOrder() method, test them each using database directly.
        // 1.数据库没查到的情况
        String orderId = orderService.generateOrderId(pid, uid, price);
        OrderIdWrapper wrapper = new OrderIdWrapper(orderId);
        Order order = orderService.payOrder(wrapper);
        System.out.println("数据库没查到时：");
        System.out.println(order);
        // 2.数据库查到了，但是没有查到token
        wrapper = new OrderIdWrapper("1566889417244.175486.176467546.194.110");
        order = orderService.payOrder(wrapper);
        System.out.println("数据库查到了，但是数据库中没有token：");
        System.out.println(order);
        // 3.数据库查到了，有token
        order = orderService.payOrder(wrapper);
        System.out.println("数据库查到了，并且已经有token：");
        System.out.println(order);

    }
}
