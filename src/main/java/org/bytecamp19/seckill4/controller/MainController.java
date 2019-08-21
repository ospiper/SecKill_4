package org.bytecamp19.seckill4.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.bytecamp19.seckill4.cache.OrderMessage;
import org.bytecamp19.seckill4.entity.*;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.service.OrderService;
import org.bytecamp19.seckill4.service.ProductService;
import org.bytecamp19.seckill4.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */

@RestController
@RequestMapping("/")
public class MainController {
    private Logger logger = LoggerFactory.getLogger(MainController.class);
    @Value("${app.resetToken}")
    private String resetToken;
    private SessionService sessionService;
    private ProductService productService;
    private OrderService orderService;

    public MainController(SessionService sessionService, ProductService productService, OrderService orderService) {
        this.sessionService = sessionService;
        this.productService = productService;
        this.orderService = orderService;
    }

    private Session getSession(String sessionId, Integer uid) throws ForbiddenException {
        Session session = sessionService.getSession(sessionId);
        logger.debug("session = " + session);
        if (session == null) {
            throw new ForbiddenException("Session not found");
        }
        if (uid != null && session.getUid() != uid) {
            throw new ForbiddenException("Invalid uid");
        }
        return session;
    }

    @GetMapping("product")
    public Product getProduct(@Param("pid") Integer pid,
                              @RequestHeader("sessionid") String sessionId) throws ForbiddenException {
        if (pid == null) {
            throw new ForbiddenException("pid not given");
        }
        logger.info("Params: pid: " + pid);
        getSession(sessionId, null);
        Product ret = productService.getProduct(pid);
        if (ret == null) {
            throw new ForbiddenException("Product not found");
        }
        logger.debug(ret.toString());
        return ret;
    }

    @PostMapping("order")
    public JSONObject placeOrder(@RequestBody JSONObject json,
                                 @RequestHeader("sessionid") String sessionId)
            throws ForbiddenException {
        Integer pid = json.getInteger("pid");
        Integer uid = json.getInteger("uid");
        if (uid == null || pid == null || sessionId == null || sessionId.isEmpty()) {
            throw new ForbiddenException("pid / uid / session not given");
        }
        logger.info("Params: pid: " + pid + ", uid: " + uid);
        // Check session
        getSession(sessionId, uid);
        logger.debug("pid = " + pid);
        logger.debug("uid = " + uid);

        // Check product
        Product product = productService.getProduct(pid);
        logger.debug("Product = " + product);
        if (product == null) {
            throw new ForbiddenException("Product not found");
        }

        // Place an order
        OrderMessage order = orderService.placeOrder(uid, product);
        // Returns null if there is no remaining products

        JSONObject ret = new JSONObject();
        if (order == null) {
            ret.put("code", 1);
            return ret;
        }
        ret.put("code", 0);
        ret.put("order_id", order.getOrder_id());
        return ret;
    }

    @PostMapping("pay")
    public JSONObject payOrder(@RequestBody JSONObject json,
                               @RequestHeader("sessionid") String sessionId)
            throws ForbiddenException {
        Integer uid = json.getInteger("uid");
        Integer price = json.getInteger("price");
        String orderId = json.getString("order_id");
        if (uid == null || price == null || orderId == null || sessionId == null || sessionId.isEmpty()) {
            throw new ForbiddenException("pid / price / order_id / session id not given");
        }
        logger.info("Params: uid: " + uid + ", price: " + price + ", order_id: " + orderId);
        OrderIdWrapper id = orderService.validateOrderId(orderId, uid, price);
        if (id == null) {
            throw new ForbiddenException("Invalid order_id");
        }
        // TODO: validate session
        getSession(sessionId, uid);

        Order o = orderService.payOrder(id);
        JSONObject ret = new JSONObject();
        if (o != null) {
            if (o.getStatus() == Order.PAID) {
                ret.put("code", 0);
                ret.put("token", o.getToken());
            }
            else {
                ret.put("code", 1);
            }
        }
        else {
            ret.put("code", 1);
        }
        return ret;
    }

    @GetMapping("result")
    public JSONObject getResult(@RequestBody JSONObject json,
                                @RequestHeader("sessionid") String sessionId)
            throws ForbiddenException {
        Integer uid = json.getInteger("uid");
        if (uid == null){
            throw new ForbiddenException("uid not given");
        }
        logger.info("Params: uid: " + uid);
        JSONObject ret = new JSONObject();
        List<OrderResult> data = orderService.getOrdersByUid(uid);
        ret.put("data", data);
        // TODO: validate session
        getSession(sessionId, uid);
        return ret;
    }

    @PostMapping("reset")
    public JSONObject reset(@RequestBody JSONObject json) throws ForbiddenException {
        String token = json.getString("token");
        if (token == null){
            throw new ForbiddenException("token not given");
        }
        if (!token.equals(resetToken)) {
            throw new ForbiddenException("Reset token mismatched");
        }
        JSONObject ret = new JSONObject();
        orderService.reset();
        if (token.equals(resetToken)){
            ret.put("code", 0);
        } else {
            ret.put("code", 1);
        }
        return ret;
    }
}
