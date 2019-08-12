package org.bytecamp19.seckill4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.bytecamp19.seckill4.entity.OrderResult;
import org.bytecamp19.seckill4.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by LLAP on 2019/8/5.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Component
public interface OrderMapper extends BaseMapper<Order> {

    @Select("select a.*, b.detail from (select * from orders where uid = #{uid}) a left join products b on a.pid = b.pid")
    List<OrderResult> getOrdersByUid(int uid);
}
