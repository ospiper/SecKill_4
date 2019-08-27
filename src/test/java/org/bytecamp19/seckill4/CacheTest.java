package org.bytecamp19.seckill4;

import org.bytecamp19.seckill4.cache.InventoryManager;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.service.ProductService;
import org.junit.After;
import org.junit.Before;
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
    @Autowired
    private InventoryManager inventoryManager;

    @Before
    public void before(){
        inventoryManager.clearInventory();
    }

    @Test
    public void redisTest() {
        final long pid = 177620431;
        Product p = productService.getProduct(pid);
        int count = p.getCount();
        Product p2 = productService.getProduct(pid);
        System.out.println(p);
        System.out.println(p2);
        assertEquals(pid, p.getPid());
        assertEquals(pid, p2.getPid());
        inventoryManager.decInventory(pid);
        Product p3 = productService.getProduct(pid);
        assertEquals(pid, p3.getPid());
        assertEquals(count - 1, p3.getCount());
    }


}
