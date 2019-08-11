package org.bytecamp19.seckill4.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.bytecamp19.seckill4.entity.Order;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Created by LLAP on 2019/8/5.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class OrderService {
    @Value("${app.debug.enabled}")
    private boolean debug;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderMapper orderMapper;

    private static Random random = new Random();

    /**
     * Generate an order_id with given arguments
     * @param pid product id
     * @param uid user id
     * @param price product price
     * @return order_id
     */
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
            if (buffer.charAt(i) != '.') check ^= buffer.charAt(i) - '0';
        }
        buffer.append(check ^ price);
        if (debug) {
            System.out.println("Generating order id " + buffer);
        }
        return buffer.toString();
    }

    /**
     * Validate Order_id
     * @param orderId id of the order
     * @param uid uid of the order
     * @param price price of the product
     * @return pid of the product, &lt; 0 if not valid; -1 (length), -2(uid), -3(check / price)
     */
    public int validateOrderId(String orderId, int uid, int price) {
        String[] segments = orderId.split("\\.");
        if (debug) {
            System.out.println("Arg count = " + segments.length);
            System.out.println("Validating " + orderId);
        }
        if (segments.length != 5) return -1;

        long r_timestamp = Long.parseLong(segments[0]);
        int r_uid = Integer.parseInt(segments[1]);
        if (debug) {
            System.out.println("Timestamp = " + r_timestamp);
            System.out.println("UID = " + r_uid);
        }
        if (uid != r_uid) return -2;
        int r_pid = Integer.parseInt(segments[2]);
        int r_rand = Integer.parseInt(segments[3]);
        int r_check = Integer.parseInt(segments[4]);
        if (debug) {
            System.out.println("PID = " + r_pid);
            System.out.println("Rand = " + r_rand);
            System.out.println("Check = " + r_check);
        }
        // validate check pattern
        int check = 0;
        for (int index = 0; index < segments.length - 1; ++index) {
            String s = segments[index];
            for (int i = 0; i < s.length(); ++i) {
                check ^= s.charAt(i) - '0';
            }
        }
        check ^= price;
        if (debug) {
            System.out.println("Expected check = " + check);
        }
        if (check != r_check) return -3;
        return r_pid;
    }

    public Order placeOrder(int uid, Product p) throws ForbiddenException {
        // Check duplicated order
        Order preOrder = orderMapper.selectOne(new QueryWrapper<Order>()
                .eq("uid", uid)
                .eq("pid", p.getPid()));
        if (preOrder != null) {
            throw new ForbiddenException("Duplicate order (uid, pid)");
        }
        // TODO: update inventory

        // Create order
        String orderId = generateOrderId(p.getPid(), uid, p.getPrice());
        Order order = new Order();
        order.setOrder_id(orderId);
        order.setPid(p.getPid());
        order.setPrice(p.getPrice());
        order.setUid(uid);
        int rowCount = orderMapper.insert(order);
        if (rowCount != 1) throw new ForbiddenException("Cannot place order");
        return order;
    }

    public Order payOrder(String orderId) {

        return null;
    }
}
