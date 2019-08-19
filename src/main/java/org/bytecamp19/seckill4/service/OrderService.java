package org.bytecamp19.seckill4.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.Getter;
import org.bytecamp19.seckill4.cache.*;
import org.bytecamp19.seckill4.entity.OrderIdWrapper;
import org.bytecamp19.seckill4.entity.OrderResult;
import org.bytecamp19.seckill4.entity.Order;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Created by LLAP on 2019/8/5.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class OrderService {
    private Logger logger = LoggerFactory.getLogger(OrderService.class);
    private ProductService productService;
    private OrderMapper orderMapper;
    private OrderLimitManager limitManager;
    private InventoryManager inventoryManager;
    private RedisMessageQueue mq;

    private static Random random = new Random();

    public OrderService(ProductService productService, OrderMapper orderMapper,
                        OrderLimitManager limitManager, InventoryManager inventoryManager,
                        RedisMessageQueue mq) {
        this.productService = productService;
        this.orderMapper = orderMapper;
        this.limitManager = limitManager;
        this.inventoryManager = inventoryManager;
        this.mq = mq;
    }



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
        logger.debug("Generating order id " + buffer);
        return buffer.toString();
    }

    /**
     * Validate Order_id
     * @param orderId id of the order
     * @param uid uid of the order
     * @param price price of the product
     * @return pid of the product, &lt; 0 if not valid; -1 (length), -2(uid), -3(check / price)
     */
    public OrderIdWrapper validateOrderId(String orderId, int uid, int price) {
        OrderIdWrapper id = null;
        try {
            id = new OrderIdWrapper(orderId);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
        if (id.getUid() != uid || id.getPrice() != price) return null;
        return id;
    }

    public OrderMessage placeOrder(int uid, Product p) throws ForbiddenException {
        // Check duplicated order
//        Order preOrder = orderMapper.selectOne(new QueryWrapper<Order>()
//                .eq("uid", uid)
//                .eq("pid", p.getPid()));
//        if (preOrder != null) {
//            throw new ForbiddenException("Duplicate order (uid, pid)");
//        }
        // Check limits
        int limit = limitManager.checkLimit(p.getPid(), uid);
        if (limit == -1) throw new ForbiddenException("Cannot check limits");
        if (limit == 0) return null;

        // Update inventory
        int inv = inventoryManager.decInventory(p.getPid());

        if (inv == -1) {
            // Recover limit (it is useful in real life but not necessary in this scenario)
            limitManager.removeLimit(p.getPid(), uid);
            return null;
        }
        if (inv == -2) {
            throw new ForbiddenException("Unexpected order");
        }
        // Create order and push to MQ
        String orderId = generateOrderId(p.getPid(), uid, p.getPrice());
        OrderMessage ret = new OrderMessage();
        ret.setPid(p.getPid());
        ret.setUid(uid);
        ret.setPrice(p.getPrice());
        ret.setOrder_id(orderId);
        mq.emit(ret);

//        Order order = new Order();
//        order.setOrder_id(orderId);
//        order.setPid(p.getPid());
//        order.setPrice(p.getPrice());
//        order.setUid(uid);
//        int rowCount = orderMapper.insert(order);
//        if (rowCount != 1) throw new ForbiddenException("Cannot place order");
        return ret;
    }

    @Cacheable(
            key = "'order:' + #orderId",
            value = "orderCache",
            cacheManager = "cacheManager"
    )
    public Order getOrder(String orderId) {
        return orderMapper.selectById(orderId);
    }

    public String getToken(OrderIdWrapper orderId) {
        return "";
    }

    public Order payOrder(OrderIdWrapper orderId) {
        Order o = getOrder(orderId.getOrderId());
        if (o == null) {
            Order ret = new Order();
            ret.setStatus(Order.PAID);
            ret.setOrder_id(orderId.getOrderId());
            // TODO: 手动获取token，返回并推送到redis
            String token = getToken(orderId);
            ret.setToken(token);
            PayMessage message = new PayMessage();
            message.setOrder_id(orderId.getOrderId());
            message.setToken(token);
            mq.emit(message);
            return ret;
        }
        else {
            o.setStatus(Order.PAID);
            if (o.getToken() != null && !o.getToken().isEmpty()) {

            }
            else {
                // TODO: 手动获取token并更新数据库
            }
            return o;
        }
        // TODO: 问题：应该在下订单时就获取付款id，还是等到有支付请求时才获取
        // TODO: 或者：通过uid和price可以检验order_id的合法性，所以
        // TODO: 1. 下订单时扣减完库存直接返回订单id，把订单加入到队列里慢慢写入，写入数据库的时候直接获取到支付id
        // TODO: 2. 支付时如果order_id合法，先查询有没有写数据，如果查询到就直接返回，如果没查询到就按照用户提交的数据发送http请求（Spring WebFlux WebClient）
        // TODO: 3. 支付时如果没查到order，需要向队列推送一条标记，该订单已经支付过（并记录支付token），在worker写数据时查询该标记并更新数据。
    }

    public List<OrderResult> getOrdersByUid(int uid){
        return orderMapper.getOrdersByUid(uid);
    }

    public void reset() {
        // Clear inventories
        inventoryManager.clearInventory();
        // Clear all order limits
        limitManager.clearLimits();
        // TODO: 清除order数据库
        // TODO: 清除消息队列的所有信息
    }
}
