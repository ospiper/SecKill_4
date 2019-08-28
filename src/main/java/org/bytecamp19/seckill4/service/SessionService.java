package org.bytecamp19.seckill4.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.bytecamp19.seckill4.cache.LayeringCache;
import org.bytecamp19.seckill4.cache.LayeringCacheManager;
import org.bytecamp19.seckill4.entity.Session;
import org.bytecamp19.seckill4.interceptor.costlogger.CostLogger;
import org.bytecamp19.seckill4.mapper.SessionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class SessionService {
    private Logger logger = LoggerFactory.getLogger(SessionService.class);

    private SessionMapper sessionMapper;
    private LayeringCacheManager cacheManager;
    private RedisTemplate<Object, Long> stringLongRedisTemplate;
    private HashOperations<Object, Object, Long> hashOperations;
    private static final String hashName = "sessionCache";

    public SessionService(SessionMapper sessionMapper, LayeringCacheManager cacheManager,
                          RedisTemplate<Object, Long> stringLongRedisTemplate) {
        this.sessionMapper = sessionMapper;
        this.cacheManager = cacheManager;
        this.stringLongRedisTemplate = stringLongRedisTemplate;
        this.hashOperations = stringLongRedisTemplate.opsForHash();
    }

//    @CostLogger(LEVEL = CostLogger.Level.WARN)
//    @DS("slave")
//    public Session getSession(String sessionid) {
//        LayeringCache cache = (LayeringCache)cacheManager.getCache("productCache");
//        Session ret = null;
//        if (cache != null) {
//            Cache.ValueWrapper val = cache.get("session:" + sessionid);
//            if (val != null) ret = (Session)val.get();
//            // if cache misses
//            if (ret == null) {
//                long start = System.currentTimeMillis();
//                ret = sessionMapper.selectOne(
//                        new QueryWrapper<Session>()
//                                .eq("sessionid", sessionid)
//                );
//                logger.warn("SQL query {} ms", (System.currentTimeMillis() - start));
//                if (ret != null) {
//                    cache.put("session:" + sessionid, ret);
//                }
//            }
//        }
//        return ret;
//    }
    @CostLogger(LEVEL = CostLogger.Level.WARN)
    @DS("slave")
    public Session getSession(String sessionid) {
        Long uid = hashOperations.get(hashName, sessionid);
        if (uid == null) {
            Session ret = sessionMapper.selectOne(
                    new QueryWrapper<Session>()
                        .eq("sessionid", sessionid)
            );
            if (ret != null) {
                hashOperations.put(hashName, sessionid, ret.getUid());
                return ret;
            }
            else return null;
        }
        else {
            Session ret = new Session();
            ret.setSessionid(sessionid);
            ret.setUid(uid);
            return ret;
        }
    }

}
