package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class ProductService {
    @Value("${app.debug.enabled}")
    private boolean debug;
    @Autowired
    private ProductMapper productMapper;

    public Product getProduct(int pid) {
        return productMapper.selectById(pid);
    }
}
