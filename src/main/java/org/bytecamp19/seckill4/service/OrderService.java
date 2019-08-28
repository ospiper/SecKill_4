package org.bytecamp19.seckill4.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Data;
import lombok.Getter;
import org.bytecamp19.seckill4.cache.*;
import org.bytecamp19.seckill4.entity.OrderIdWrapper;
import org.bytecamp19.seckill4.entity.OrderResult;
import org.bytecamp19.seckill4.entity.Order;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.interceptor.costlogger.CostLogger;
import org.bytecamp19.seckill4.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    private WebClient webClient;
    @Value("${app.tokenServer}")
    private String tokenServer;
    private static Random random = new Random();

    public OrderService(ProductService productService, OrderMapper orderMapper,
                        OrderLimitManager limitManager, InventoryManager inventoryManager,
                        RedisMessageQueue mq) {
        this.productService = productService;
        this.orderMapper = orderMapper;
        this.limitManager = limitManager;
        this.inventoryManager = inventoryManager;
        this.mq = mq;
        this.webClient = WebClient.builder().build();
    }

    /**
     * Generate an order_id with given arguments
     * @param pid product id
     * @param uid user id
     * @param price product price
     * @return order_id
     */
    public String generateOrderId(long pid, long uid, int price) {
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
    public OrderIdWrapper validateOrderId(String orderId, long uid, long price) {
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

    /**
     * Place an order
     * @param uid user id
     * @param p product entity
     * @return An OrderMessage that has been emitted to MQ, null if not ordered.
     * @throws ForbiddenException
     */
//    @CostLogger(LEVEL = CostLogger.Level.ERROR)
    public OrderMessage placeOrder(long uid, Product p) throws ForbiddenException {
        // Check limits
        int limit = limitManager.checkLimit(p.getPid(), uid);
        if (limit == -1) throw new ForbiddenException("Cannot check limits");
        if (limit == 0) throw new ForbiddenException("Repeat orders");

        // Update inventory
        int inv = inventoryManager.decInventory(p.getPid());

        if (inv == -1) {
            // Recover limit (it is useful in real life but not necessary in this scenario)
//            limitManager.removeLimit(p.getPid(), uid);
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
        logger.debug(mq.emit(ret) + " message emitted");

//        Order order = new Order();
//        order.setOrder_id(orderId);
//        order.setPid(p.getPid());
//        order.setPrice(p.getPrice());
//        order.setUid(uid);
//        int rowCount = orderMapper.insert(order);
//        if (rowCount != 1) throw new ForbiddenException("Cannot place order");
        return ret;
    }

    /**
     * Get Order from cache/DB
     * @param orderId order id wrapper
     * @return Order
     */
    @Cacheable(
            key = "'order:' + #orderId",
            value = "orderCache",
            cacheManager = "cacheManager"
    )
    @DS("slave")
    public Order getOrder(OrderIdWrapper orderId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId.getOrderId());
        wrapper.eq("uid", orderId.getUid());
        wrapper.eq("price", orderId.getPrice());
        wrapper.eq("pid", orderId.getPid());
        return orderMapper.selectOne(wrapper);
    }

    /**
     * Request a pay token from token server
     * @param orderId order id wrapper
     * @return token
     */
    public String getToken(OrderIdWrapper orderId) {
        JSONObject reqData = new JSONObject();
        reqData.put("order_id", orderId.getOrderId());
        reqData.put("uid", orderId.getUid());
        reqData.put("price", orderId.getPrice());
        Mono<JSONObject> res = webClient.post()
                .uri(tokenServer + "/token")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(reqData.toJSONString()))
                .retrieve()
                .bodyToMono(JSONObject.class);
        JSONObject retJson = res.block();
        return retJson == null ? null : retJson.getString("token");
    }

    /**
     * Pay an order
     * @param orderId order id
     * @return Order entity (might be an uncompleted one)
     */
    public Order payOrder(OrderIdWrapper orderId) {
        Order o = getOrder(orderId);
        if (o == null) {
            Order ret = new Order();
            ret.setStatus(Order.PAID);
            ret.setOrder_id(orderId.getOrderId());
            // 手动获取token，返回并推送到redis
            String token = getToken(orderId);
            ret.setToken(token);
            PayMessage message = new PayMessage();
            message.setOrder_id(orderId.getOrderId());
            message.setToken(token);
            logger.debug("Token emit result: " + mq.emit(message));
            return ret;
        }
        else {
            if (o.getStatus() == Order.PAID) {
                return o;
            }
            // 有查询到，直接更新数据库即可
            o.setStatus(Order.PAID);
            logger.debug("Found order in db");
            UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
            if (o.getToken() == null || o.getToken().isEmpty()) {
                // token为空，需要手动获取token
                logger.debug("Requesting token");
                o.setToken(getToken(orderId));
//                updateWrapper.set("token", o.getToken());
            }
//            orderMapper.updateById(o);
            orderMapper.update(o, updateWrapper.eq("order_id", orderId.getOrderId()));
            return o;
        }
        // TODO: 问题：应该在下订单时就获取付款id，还是等到有支付请求时才获取
        // TODO: 或者：通过uid和price可以检验order_id的合法性，所以
        // TODO: 1. 下订单时扣减完库存直接返回订单id，把订单加入到队列里慢慢写入，写入数据库的时候直接获取到支付id
        // TODO: 2. 支付时如果order_id合法，先查询有没有写数据，如果查询到就直接返回，如果没查询到就按照用户提交的数据发送http请求（Spring WebFlux WebClient）
        // TODO: 3. 支付时如果没查到order，需要向队列推送一条标记，该订单已经支付过（并记录支付token），在worker写数据时查询该标记并更新数据。
    }

    /**
     * Get results of specified user
     * @param uid user id
     * @return a list of orders that the user has placed
     */
    public List<OrderResult> getOrdersByUid(long uid) {
        mq.waitForConsumer();
        return orderMapper.getOrdersByUid(uid);
    }

    /**
     * Reset all status including:
     * - clear all the orders from db
     * - clear pay message queue & order messasge queue
     * - clear inventories of all products
     * - clear all order limits for all users
     *
     */
    public void reset() {
        // Clear inventory
        inventoryManager.clearInventory();
        // Clear order limits
        limitManager.clearLimits();
        // Clear orders table
        orderMapper.delete(null);
        mq.clear();
    }
}
