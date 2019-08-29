package org.bytecamp19.seckill4.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Data
@TableName("orders")
public class Order implements Serializable {
    public static final int UNPAID = 0;
    public static final int PAID = 1;

    @TableId
    @JSONField(name = "order_id")
    protected String orderId;
    protected long uid;
    protected long pid;
    protected int price;
    protected int status = 0;
    protected String token;

    @Override
    public String toString() {
        return "Order{" +
                "order_id='" + orderId + '\'' +
                ", uid=" + uid +
                ", pid=" + pid +
                ", price=" + price +
                ", status=" + status +
                ", token='" + token + '\'' +
                '}';
    }
}
