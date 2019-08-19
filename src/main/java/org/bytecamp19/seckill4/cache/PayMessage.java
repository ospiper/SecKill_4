package org.bytecamp19.seckill4.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by LLAP on 2019/8/19.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@Data
public class PayMessage implements Serializable {
    private String order_id;
    private String token;
}
