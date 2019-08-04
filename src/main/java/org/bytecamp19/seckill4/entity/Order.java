package org.bytecamp19.seckill4.entity;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
public class Order {
    private String order_id;
    private int uid;
    private int pid;
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

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
