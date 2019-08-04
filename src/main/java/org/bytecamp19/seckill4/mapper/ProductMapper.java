package org.bytecamp19.seckill4.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.bytecamp19.seckill4.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */

@Mapper
@Component(value = "productMapper")
public interface ProductMapper {
    Product findByPid(int pid);
}
