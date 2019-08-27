package org.bytecamp19.seckill4.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by LLAP on 2019/8/5.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Data
@TableName("sessions")
public class Session implements Serializable {
    @TableId
    private String sessionid;
    private long uid;

    @Override
    public String toString() {
        return "Session{" +
                "uid=" + uid +
                ", sessionid='" + sessionid + '\'' +
                '}';
    }

}
