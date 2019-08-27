package org.bytecamp19.seckill4.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class OrderResult implements Serializable {
    private long uid;
    private long pid;
    private String detail;
    private String order_id;
    private int price;
    private int status;
    private String token;
}
