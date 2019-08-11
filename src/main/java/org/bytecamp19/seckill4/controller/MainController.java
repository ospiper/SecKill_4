package org.bytecamp19.seckill4.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.bytecamp19.seckill4.entity.Order;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.service.OrderService;
import org.bytecamp19.seckill4.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public JSONObject placeOrder(@RequestBody JSONObject json) throws ForbiddenException {
        Integer pid = json.getInteger("pid");
        Integer uid = json.getInteger("uid");
        if (uid == null || pid == null) {
            throw new ForbiddenException("pid or uid not given");
        }
        // Place an order
        Order order = orderService.placeOrder(pid, uid);
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
    public HashMap<String, Object> payOrder() throws ForbiddenException {
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
