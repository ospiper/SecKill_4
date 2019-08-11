package org.bytecamp19.seckill4.config;

import org.bytecamp19.seckill4.interceptor.MainInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */

@Configuration
public class InterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private MainInterceptor mainInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mainInterceptor).addPathPatterns("/*");
    }
}
