package org.bytecamp19.seckill4.entity;

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
    private String order_id;
    private long uid;
    private long pid;
    private int price;
    private int status = 0;
    private String token;

    @Override
    public String toString() {
        return "Order{" +
                "order_id='" + order_id + '\'' +
                ", uid=" + uid +
                ", pid=" + pid +
                ", price=" + price +
                ", status=" + status +
                ", token='" + token + '\'' +
                '}';
    }
}
