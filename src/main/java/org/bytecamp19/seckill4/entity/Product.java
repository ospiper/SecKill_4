package org.bytecamp19.seckill4.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Data
@TableName("products")
public class Product {
    @TableId
    private int pid;
    private int count;
    private int price;
    private String detail;

    @Override
    public String toString() {
        return "Product{" +
                "pid=" + pid +
                ", count=" + count +
                ", price=" + price +
                ", detail='" + detail + '\'' +
                '}';
    }
}
