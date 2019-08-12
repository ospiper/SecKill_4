package org.bytecamp19.seckill4.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
    @Value("${app.debug.enabled}")
    private boolean debug;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getHeader("x-forwarded-for");
        String ua = request.getHeader("user-agent");
        String sessionid = request.getHeader("sessionid");
        if (debug) {
            System.out.println("Prehandling " + request.getRequestURI());
            printHeaders(ip, ua, sessionid);
        }

        // Check headers
        if (ip == null || ua == null || sessionid == null) {
            if (debug) System.err.println("IP / UA / SessionId not found");
            response.setStatus(403);
            return false;
        }

        // Check ip patterns
        Matcher v4Match = ipv4.matcher(ip + ".");
        if (!v4Match.matches()) {
            Matcher v6Match = ipv6.matcher(ip + ":");
            if (!v6Match.matches()) {
                if (debug) System.err.println("IP is neither v4 nor v6");
                response.setStatus(403);
                return false;
            }
        }

        // Check ua
        // TODO: Find better ways of filtering user agent
        String uan = ua.toLowerCase();
        if (uan.contains("curl")
                || uan.contains("spider")
                || uan.contains("wget")) {
            if (debug) System.err.println("UA blocked");
            response.setStatus(403);
            return false;
        }

        // Check session format
        if (sessionid.length() != 32) {
            if (debug) System.err.println("Wrong sessionid format");
            response.setStatus(403);
            return false;
        }
        Matcher md5Match = md5.matcher(sessionid);
        if (!md5Match.matches()) {
            if (debug) System.err.println("Wrong md5 format");
            response.setStatus(403);
            return false;
        }
        return true;
    }

    private void printHeaders(String ip, String ua, String sid) {
        System.out.println("IP = " + ip);
        System.out.println("UA = " + ua);
        System.out.println("Session id = " + sid);
    }
}
