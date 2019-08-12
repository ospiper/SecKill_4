package org.bytecamp19.seckill4.error;

import lombok.ToString;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@ToString
public class ForbiddenException extends Exception {
    public ForbiddenException(String message) {
        super(message);
    }
}
