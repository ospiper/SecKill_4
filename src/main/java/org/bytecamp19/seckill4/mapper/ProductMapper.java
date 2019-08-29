package org.bytecamp19.seckill4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.interceptor.costlogger.CostLogger;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Component
public interface ProductMapper extends BaseMapper<Product> {

    @Select("select * from products_part_${remainder};")
    List<Product>selectPartitionedProducts(int remainder);
}
