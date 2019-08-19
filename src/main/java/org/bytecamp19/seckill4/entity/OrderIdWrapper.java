package org.bytecamp19.seckill4.entity;

import lombok.Data;
import lombok.Getter;

/**
 * Created by LLAP on 2019/8/19.
 * Copyright (c) 2019 LLAP. All rights reserved.
 */
@Data
@Getter
public class OrderIdWrapper {
    private final String orderId;
    private final long timestamp;
    private final int uid;
    private final int pid;
    private final int rand;
    private final int price;
    private final int check;
    public OrderIdWrapper(String orderId) throws IllegalArgumentException {
        this.orderId = orderId;
        String[] segments = orderId.split("\\.");
        if (segments.length != 5) throw new IllegalArgumentException("Invalid format");
        timestamp = Long.parseLong(segments[0]);
        uid = Integer.parseInt(segments[1]);
        pid = Integer.parseInt(segments[2]);
        rand = Integer.parseInt(segments[3]);
        check = Integer.parseInt(segments[4]);
        int o_check = 0;
        for (int index = 0; index < segments.length - 1; ++index) {
            String s = segments[index];
            for (int i = 0; i < s.length(); ++i) {
                o_check ^= s.charAt(i) - '0';
            }
        }
        price = check ^ o_check;
    }
}
