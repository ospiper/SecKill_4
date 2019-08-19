package org.bytecamp19.seckill4.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.bytecamp19.seckill4.entity.Session;
import org.bytecamp19.seckill4.mapper.SessionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class SessionService {
    private Logger logger = LoggerFactory.getLogger(SessionService.class);

    private SessionMapper sessionMapper;

    public SessionService(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }

    @Cacheable(
            key = "'session:' + #sessionid",
            value = "sessionCache",
            cacheManager = "cacheManager"
    )
    public Session getSession(String sessionid, int uid) {
        return sessionMapper.selectOne(
                new QueryWrapper<Session>()
                        .eq("sessionid", sessionid)
                        .eq("uid", uid)
        );

    }

}
