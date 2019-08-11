package org.bytecamp19.seckill4.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.bytecamp19.seckill4.entity.Order;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.entity.Session;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.service.OrderService;
import org.bytecamp19.seckill4.service.ProductService;
import org.bytecamp19.seckill4.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */

@RestController
@RequestMapping("/")
public class MainController {
    @Value("${app.debug.enabled}")
    private boolean debug;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;

    @GetMapping("product")
    public Product getProduct(@Param("pid") Integer pid) throws ForbiddenException {
        if (pid == null) {
            throw new ForbiddenException("pid not given");
        }
        Product ret = productService.getProduct(pid);
        System.out.println(ret);
        if (ret == null) {
            throw new ForbiddenException("Product not found");
        }
        return ret;
    }

    @PostMapping("order")
    public JSONObject placeOrder(@RequestBody JSONObject json, HttpServletRequest request) throws ForbiddenException {
        Integer pid = json.getInteger("pid");
        Integer uid = json.getInteger("uid");
        if (uid == null || pid == null) {
            throw new ForbiddenException("pid or uid not given");
        }

        // Check session
        Session session = sessionService.getSession(request.getHeader("sessionid"), uid);
        if (debug) {
            System.out.println("pid = " + pid);
            System.out.println("uid = " + uid);
            System.out.println("session = " + session);
        }
        if (session == null) {
            throw new ForbiddenException("Session not found");
        }

        // Check product
        Product product = productService.getProduct(pid);
        if (debug) {
            System.out.println("Product = " + product);
        }
        if (product == null) {
            throw new ForbiddenException("Product not found");
        }

        // Place an order
        Order order = orderService.placeOrder(uid, product);
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
    public JSONObject payOrder(@RequestBody JSONObject json) throws ForbiddenException {
        Integer uid = json.getInteger("uid");
        Integer price = json.getInteger("price");
        String orderId = json.getString("order_id");
        if (uid == null || price == null || orderId == null) {
            throw new ForbiddenException("pid / price / order_id not given");
        }
        int pid = orderService.validateOrderId(orderId, uid, price);
        if (pid < 0) {
            throw new ForbiddenException("Invalid order_id");
        }

        return null;
    }

    @GetMapping("result")
    public JSONObject getResult() throws ForbiddenException {
        return null;
    }

    @PostMapping("reset")
    public JSONObject reset() throws ForbiddenException {
        return null;
    }
}
