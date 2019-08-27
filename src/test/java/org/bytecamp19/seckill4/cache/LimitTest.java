package org.bytecamp19.seckill4.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by LLAP on 2019/8/18.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LimitTest {
    @Autowired
    private OrderLimitManager limitManager;

    @Before
    public void before() {
        limitManager.clearLimits();
        assert limitManager.getLimits().size() == 0;
    }

    @Test
    public void limitTest() {
        long pid = 123456789;
        long uid = 5443321;
        int res1 = limitManager.checkLimit(pid, uid);
        assertEquals(1, res1);
        int res2 = limitManager.checkLimit(pid, uid);
        assertEquals(0, res2);
        Set<Long> members = limitManager.getLimit(pid);
        assertEquals(1, members.size());
        System.out.println(members);
        assertTrue(members.contains(uid));
        assertTrue(limitManager.removeLimit(pid));
        members = limitManager.getLimit(pid);
        assertEquals(0, members.size());
    }


}
