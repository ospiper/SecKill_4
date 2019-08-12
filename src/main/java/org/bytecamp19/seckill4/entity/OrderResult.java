package org.bytecamp19.seckill4.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrderResult {
    private int uid;
    private int pid;
    private String detail;
    private String order_id;
    private int price;
    private int status;
    private String token;
}
