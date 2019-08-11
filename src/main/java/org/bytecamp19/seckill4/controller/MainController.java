package org.bytecamp19.seckill4.controller;

import org.apache.ibatis.annotations.Param;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */

@RestController
@RequestMapping("/")
public class MainController {

    @Autowired
    private ProductService productService;

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
    public HashMap<String, Object> placeOrder() throws ForbiddenException {
        return null;
    }

    @PostMapping("pay")
    public HashMap<String, Object> payOrder() throws ForbiddenException {
        return null;
    }

    @GetMapping("result")
    public HashMap<String, Object> getResult() throws ForbiddenException {
        return null;
    }

    @PostMapping("reset")
    public HashMap<String, Object> reset() throws ForbiddenException {
        return null;
    }
}
