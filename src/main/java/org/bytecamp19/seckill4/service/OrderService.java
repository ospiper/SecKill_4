package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.mapper.OrderMapper;
import org.bytecamp19.seckill4.mapper.ProductMapper;
import org.bytecamp19.seckill4.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by LLAP on 2019/8/5.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
public class OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SessionMapper sessionMapper;
}
