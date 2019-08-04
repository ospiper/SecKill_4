package org.bytecamp19.seckill4;

import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

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
    public List<Product> getProduct() {
        return null;
    }

    @PostMapping("order")
    public HashMap<String, Object> placeOrder() {
        return null;
    }

    @PostMapping("pay")
    public HashMap<String, Object> payOrder() {
        return null;
    }

    @GetMapping("result")
    public HashMap<String, Object> getResult() {
        return null;
    }

    @PostMapping("reset")
    public HashMap<String, Object> reset() {
        return null;
    }
}
