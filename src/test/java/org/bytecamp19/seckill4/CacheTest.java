package org.bytecamp19.seckill4;

import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.service.ProductService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by LLAP on 2019/8/12.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheTest {
    @Autowired
    private ProductService productService;

    @Test
    public void redisTest() {
        final int pid = 177620431;
        Product p = productService.getProduct(pid);
        Product p2 = productService.getProduct(pid);
        System.out.println(p);
        System.out.println(p2);
    }

    @After
    public void clear() {

    }
}
