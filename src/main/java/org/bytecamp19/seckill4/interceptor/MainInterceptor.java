package org.bytecamp19.seckill4.interceptor;

import org.bytecamp19.seckill4.interceptor.costlogger.CostLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */

@Component
public class MainInterceptor implements HandlerInterceptor {

    private static final Pattern ipv4 = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.){4}$");
    private static final Pattern ipv6 = Pattern.compile("^(([\\da-fA-F]{1,4}):){8}$");
    private static final Pattern md5 = Pattern.compile("^([a-fA-F0-9]{32})$");
    private Logger logger = LoggerFactory.getLogger(MainInterceptor.class);

    @CostLogger(LEVEL = CostLogger.Level.DEBUG)
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        logger.debug("Prehandling " + request.getRequestURI());
        logger.info(request.getMethod() + " " + request.getRequestURI());
        String ip = request.getHeader("x-forwarded-for").split(",")[0].trim();
        String ua = request.getHeader("user-agent");
        String sessionid = request.getHeader("sessionid");
        boolean ret = checkHeaders(ip, ua, sessionid);
        if (!ret) {
            response.setStatus(403);
        }
        return ret;
    }

    private boolean checkHeaders(String ip, String ua, String sessionid) {
        printHeaders(ip, ua, sessionid);

        // Check headers
        if (ip == null || ua == null || sessionid == null) {
            logger.debug("IP / UA / SessionId not found");
            return false;
        }

        // Check ip patterns
        Matcher v4Match = ipv4.matcher(ip + ".");
        if (!v4Match.matches()) {
            Matcher v6Match = ipv6.matcher(ip + ":");
            if (!v6Match.matches()) {
                logger.debug("IP is neither v4 nor v6");
                return false;
            }
        }

        // Check ua
        // TODO: Find better ways of filtering user agent
        String uan = ua.toLowerCase();
        if (uan.contains("curl")
                || uan.contains("spider")
                || uan.contains("wget")) {
            logger.debug("UA blocked");
            return false;
        }

        // Check session format
        if (sessionid.length() != 32) {
            logger.debug("Wrong sessionid format");
            return false;
        }
        Matcher md5Match = md5.matcher(sessionid);
        if (!md5Match.matches()) {
            logger.debug("Wrong md5 format");
            return false;
        }
        return true;
    }

    private void printHeaders(String ip, String ua, String sid) {
        logger.debug("IP = " + ip);
        logger.debug("UA = " + ua);
        logger.debug("Session id = " + sid);
    }
}
