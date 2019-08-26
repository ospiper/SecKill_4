package org.bytecamp19.seckill4.cache;

import org.bytecamp19.seckill4.cache.lock.RedisDistributedLock;
import org.bytecamp19.seckill4.interceptor.costlogger.CostLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by LLAP on 2019/8/17.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@Component
@AutoConfigureAfter(RedisTemplate.class)
public class InventoryManager {
    private Logger logger = LoggerFactory.getLogger(InventoryManager.class);
    private RedisTemplate<Object, Integer> stringIntegerRedisTemplate;
    private HashOperations<Object, Object, Integer> hashOperations = null;
    private static final String hashName = "inventory";

    public InventoryManager(RedisTemplate<Object, Integer> stringIntegerRedisTemplate) {
        this.stringIntegerRedisTemplate = stringIntegerRedisTemplate;
        this.hashOperations = this.stringIntegerRedisTemplate.opsForHash();
    }

    public boolean initInventory(int pid, int count) {
        boolean ret = hashOperations.putIfAbsent(hashName, String.valueOf(pid), count);
        if (ret) logger.debug("Initialized inventory of product " + pid);
        return ret;
    }

//    public int setInventory(int pid, int count) {
//        hashOperations.put(hashName, String.valueOf(pid), count);
//        return getInventory(pid);
//    }

//    @CostLogger(LEVEL = CostLogger.Level.WARN)
    public int getInventory(int pid) {
        logger.debug("Getting inventory for " + pid);
        Object ret = hashOperations.get(hashName, String.valueOf(pid));
        logger.debug("Native inventory: " + ret);
        return ret == null ? -1 : (Integer)ret;
    }

    /**
     *
     * @param pid product id
     * @return updated inventory, -1(insufficient products), -2(product not found)
     */
//    @CostLogger(LEVEL = CostLogger.Level.WARN)
    public int decInventory(int pid) {
        String lockKey = "Inventory:" + pid;
        RedisDistributedLock redisLock = new RedisDistributedLock(stringIntegerRedisTemplate);
        int inv = -1;
        int ret = -1;
//        while (!redisLock.lock(lockKey, 1500L));

//        inv = getInventory(pid);
//        if (inv >= 1) {
            Long inc = hashOperations.increment(hashName, String.valueOf(pid), -1);
            if (inc == null) return -2;
            ret = inc.intValue();
//                ret = setInventory(pid, inv - 1);
            if (ret < 0) {
//                hashOperations.increment(hashName, String.valueOf(pid), 1).intValue();
                ret = -1;
            }
//        }
//        else if (inv == -1) {
//            ret = -2;
//        }
//        redisLock.releaseLock(lockKey);

        return ret;
    }

    public void clearInventory() {
        stringIntegerRedisTemplate.delete(hashName);
    }

    public void deleteInventory(int pid) {
        hashOperations.delete(hashName, String.valueOf(pid));
    }

    public Map<Object, Integer> getInventories() {
        // ONLY FOR TESTING, DO NOT USE IT IN THE APPLICATION!
        Map<Object, Integer> ret = new HashMap<>();
        Set<Object> keys = hashOperations.keys(hashName);
        if (keys == null) return ret;
        for (Object key : keys) {
            ret.put(key, hashOperations.get(hashName, key));
        }
        return ret;
    }
}
