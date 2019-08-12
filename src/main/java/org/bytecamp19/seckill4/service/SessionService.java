package org.bytecamp19.seckill4.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.bytecamp19.seckill4.entity.Session;
import org.bytecamp19.seckill4.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class SessionService {
    @Value("${app.debug.enabled}")
    private boolean debug;
    @Autowired
    private SessionMapper sessionMapper;

    public Session getSession(String sessionid, int uid) {
        return sessionMapper.selectOne(
                new QueryWrapper<Session>()
                        .eq("sessionid", sessionid)
                        .eq("uid", uid)
        );

    }

}
