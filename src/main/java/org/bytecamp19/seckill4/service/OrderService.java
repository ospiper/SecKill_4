package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.entity.Order;
import org.bytecamp19.seckill4.mapper.OrderMapper;
import org.bytecamp19.seckill4.mapper.ProductMapper;
import org.bytecamp19.seckill4.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

/**
 * Created by LLAP on 2019/8/5.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
public class OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SessionMapper sessionMapper;

    private static Random random;

    public String generateOrderId(int pid, int uid, int price) {
        // ${毫秒时间戳}.${uid}.${pid}.${[1-1000]随机数}.${每一位数字位异或后再异或价格}
        int rand = (int)(random.nextDouble() * 1000);
        StringBuilder buffer = new StringBuilder();
        buffer
                .append(System.currentTimeMillis()).append('.')
                .append(uid).append('.')
                .append(pid).append('.')
                .append(rand).append('.');
        int check = 0;
        for (int i = 0; i < buffer.length(); ++i) {
            if (buffer.charAt(i) != '.') check ^= (int)(buffer.charAt(i) - '0');
        }
        buffer.append(check ^ price);
        return buffer.toString();
    }

    public Order placeOrder(int pid, int uid) {

        return null;
    }
}
