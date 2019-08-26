package org.bytecamp19.seckill4.cache;

import org.bytecamp19.seckill4.interceptor.costlogger.CostLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by LLAP on 2019/8/18.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@Component
public class OrderLimitManager {
    private Logger logger = LoggerFactory.getLogger(InventoryManager.class);
    private RedisTemplate<Object, Integer> stringIntegerRedisTemplate;
    private SetOperations<Object, Integer> setOperations;
    private static final String hashName = "orderLimit:";

    public OrderLimitManager(RedisTemplate<Object, Integer> stringIntegerRedisTemplate) {
        this.stringIntegerRedisTemplate = stringIntegerRedisTemplate;
        this.setOperations = this.stringIntegerRedisTemplate.opsForSet();
    }

    /**
     * Check if the user has already ordered the product and
     * add the user to order list if not ordered.
     * @param pid product id
     * @param uid user id
     * @return 1(not ordered), 0(ordered), -1(error)
     */
//    @CostLogger(LEVEL = CostLogger.Level.WARN)
    public int checkLimit(int pid, int uid) {
        Long ret = setOperations.add(hashName + pid, uid);
        return ret == null ? -1 : ret.intValue();
    }

    public int removeLimit(int pid, int uid) {
        Long ret = setOperations.remove(hashName + pid, uid);
        return ret == null ? -1 : ret.intValue();
    }

    public Long clearLimits() {
        Set<Object> keys = stringIntegerRedisTemplate.keys(hashName + "*");
//        System.out.println(keys);
        if (keys == null) return null;
        return stringIntegerRedisTemplate.delete(keys);
    }

    public Set<Integer> getLimit(int pid) {
        return setOperations.members(hashName + pid);
    }

    public Boolean removeLimit(int pid) {
        return stringIntegerRedisTemplate.delete(hashName + pid);
    }

    public HashMap<String, Set<Integer>> getLimits() {
        HashMap<String, Set<Integer>> ret = new HashMap<>();
        Set<Object> keys = stringIntegerRedisTemplate.keys(hashName + "*");
        if (keys == null) return null;
        for (Object key : keys) {
            ret.put(key.toString(), setOperations.members(key));
        }
        return ret;
    }
}
