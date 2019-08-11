package org.bytecamp19.seckill4.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */

@Component
public class MainInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getHeader("x-forwarded-for");
        String ua = request.getHeader("user-agent");
        String sessionid = request.getHeader("sessionid");
        System.out.println(request.getRequestURI());
        if (ip == null || ua == null || sessionid == null) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}
