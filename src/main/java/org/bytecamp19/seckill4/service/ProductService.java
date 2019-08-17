package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class ProductService {
    private Logger logger = LoggerFactory.getLogger(ProductService.class);
    @Autowired
    private ProductMapper productMapper;

    @Cacheable(
            key = "'product:' + #pid",
            value = "productCache",
            cacheManager = "cacheManager"
    )
    public Product getProduct(int pid) {
        return productMapper.selectById(pid);
    }
}
