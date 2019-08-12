package org.bytecamp19.seckill4;

import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.entity.Session;
import org.bytecamp19.seckill4.mapper.ProductMapper;
import org.bytecamp19.seckill4.mapper.SessionMapper;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class EntityTest {
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private ProductMapper productMapper;

    @Test
    public void testSessionSelect() {
        String sid = "ab5c53d8e094263412ffdfe524ddba0d";
        Session s = sessionMapper.selectById(sid);
        assertEquals(s.getUid(), 175230);
        assertEquals(s.getSessionid(), sid);
    }

    @Test
    public void testSessionCount() {
        assertEquals(sessionMapper.selectCount(null).intValue(), 5000000);
    }

    @Test
    public void testProductSelect() {
        final int pid = 177620431;
        Product p = productMapper.selectById(pid);
        System.out.println(p);
        assertEquals(p.getPid(), pid);
    }

    @Test
    public void testProductCount() {
        assertEquals(productMapper.selectCount(null).intValue(), 50000);
    }
}
