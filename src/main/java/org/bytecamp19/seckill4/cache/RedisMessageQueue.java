package org.bytecamp19.seckill4.cache;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by LLAP on 2019/8/19.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@Component
public class RedisMessageQueue {
    private Logger logger = LoggerFactory.getLogger(RedisMessageQueue.class);
    private StringRedisTemplate stringRedisTemplate;
    private ListOperations<String, String> listOperations;
    private HashOperations<String, String, String> hashOperations;

    private static final String queueName = "orderQueue";
    private static final String payHashName = "paidOrder";

    public RedisMessageQueue(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.listOperations = this.stringRedisTemplate.opsForList();
        this.hashOperations = this.stringRedisTemplate.opsForHash();
    }

    public int emit(OrderMessage message) {
        Long ret = listOperations.leftPush(queueName, JSON.toJSONString(message));
        return ret == null ? -1 : ret.intValue();
    }

    public Boolean emit(PayMessage message) {
        return hashOperations.putIfAbsent(payHashName, message.getOrder_id(), message.getToken());
    }

}
