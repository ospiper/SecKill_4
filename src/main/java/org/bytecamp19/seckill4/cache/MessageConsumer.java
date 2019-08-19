package org.bytecamp19.seckill4.cache;

import org.bytecamp19.seckill4.entity.Order;
import org.bytecamp19.seckill4.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by LLAP on 2019/8/19.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */

public class MessageConsumer extends Thread {
    private Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private RedisTemplate<Object, Object> stringKeyRedisTemplate;
    private ListOperations<Object, Object> listOperations;
    private HashOperations<Object, Object, Object> hashOperations;
    private OrderMapper orderMapper;

    private static final String queueName = "orderQueue";
    private static final String payHashName = "paidOrder";

    public MessageConsumer(RedisTemplate<Object, Object> stringKeyRedisTemplate, OrderMapper orderMapper) {
        this.stringKeyRedisTemplate = stringKeyRedisTemplate;
        this.listOperations = this.stringKeyRedisTemplate.opsForList();
        this.hashOperations = this.stringKeyRedisTemplate.opsForHash();
        this.orderMapper = orderMapper;
    }

    @Override
    public void run() {
        OrderMessage message = null;
        while (true) {
            message = (OrderMessage)listOperations.rightPop(queueName);
            if (message != null) {
                Order o = new Order();
                o.setUid(message.getUid());
                o.setPid(message.getPid());
                o.setPrice(message.getPrice());
                o.setOrder_id(message.getOrder_id());
                Object payToken = hashOperations.get(payHashName, message.getOrder_id());
                if (payToken != null) {
                    o.setStatus(Order.PAID);
                    o.setToken(payToken.toString());
                }
                else {
                    o.setStatus(Order.UNPAID);
                    // TODO: retrieve a token and set
                }
                int result = orderMapper.insert(o);
                if (result > 0) {
                    payToken = hashOperations.get(payHashName, message.getOrder_id());
                    if (payToken != null && o.getStatus() == Order.UNPAID) {
                        o.setStatus(Order.PAID);
                        orderMapper.updateById(o);
                    }
                }
                hashOperations.delete(payHashName, message.getOrder_id());
            }
        }

    }
}
