package org.bytecamp19.seckill4.cache;

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
    private RedisTemplate<Object, Long> stringLongRedisTemplate;
    private SetOperations<Object, Long> setOperations;
    private static final String hashName = "orderLimit:";

    public OrderLimitManager(RedisTemplate<Object, Long> stringLongRedisTemplate) {
        this.stringLongRedisTemplate = stringLongRedisTemplate;
        this.setOperations = this.stringLongRedisTemplate.opsForSet();
    }

    /**
     * Check if the user has already ordered the product and
     * add the user to order list if not ordered.
     * @param pid product id
     * @param uid user id
     * @return 1(not ordered), 0(ordered), -1(error)
     */
//    @CostLogger(LEVEL = CostLogger.Level.WARN)
    public int checkLimit(long pid, long uid) {
        Long ret = setOperations.add(hashName + pid, uid);
        return ret == null ? -1 : ret.intValue();
    }

    public int removeLimit(long pid, long uid) {
        Long ret = setOperations.remove(hashName + pid, uid);
        return ret == null ? -1 : ret.intValue();
    }

    public Long clearLimits() {
        Set<Object> keys = stringLongRedisTemplate.keys(hashName + "*");
//        System.out.println(keys);
        if (keys == null) return null;
        return stringLongRedisTemplate.delete(keys);
    }

    public Set<Long> getLimit(long pid) {
        return setOperations.members(hashName + pid);
    }

    public Boolean removeLimit(long pid) {
        return stringLongRedisTemplate.delete(hashName + pid);
    }

    public HashMap<String, Set<Long>> getLimits() {
        HashMap<String, Set<Long>> ret = new HashMap<>();
        Set<Object> keys = stringLongRedisTemplate.keys(hashName + "*");
        if (keys == null) return null;
        for (Object key : keys) {
            ret.put(key.toString(), setOperations.members(key));
        }
        return ret;
    }
}
