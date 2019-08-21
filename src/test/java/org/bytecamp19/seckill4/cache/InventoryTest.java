package org.bytecamp19.seckill4.cache;

import org.bytecamp19.seckill4.cache.InventoryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by LLAP on 2019/8/17.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class InventoryTest {
    @Autowired
    private InventoryManager inventoryManager;

    @Before
    public void before(){
        inventoryManager.clearInventory();
        assert inventoryManager.getInventories().size() == 0;
    }

    @Test
    public void inventoryTest() {
        int pid = 102233011;
        int count = 666333;
        int inv = inventoryManager.getInventory(pid);
        assertEquals(-1, inv);
        boolean set = inventoryManager.initInventory(pid, count);
        int inv2 = inventoryManager.getInventory(pid);
        assertEquals(count, inv2);
        int inv3 = inventoryManager.decInventory(pid);
        assertEquals(inv2 - 1, inv3);
    }


}
