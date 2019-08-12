package org.bytecamp19.seckill4.error;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by LLAP on 2019/8/11.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${app.debug.enabled}")
    private boolean debug;
    @Value("${app.debug.printStack}")
    private boolean printStack;
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, error, ex));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<Object>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<Object> handleForbidden(
            ForbiddenException ex) {
        ApiError err = new ApiError(HttpStatus.FORBIDDEN);
        err.setMessage(ex.getMessage());
        if (printStack) ex.printStackTrace();
        return buildResponseEntity(err);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception ex) {
        ApiError err = new ApiError(HttpStatus.FORBIDDEN);
        err.setMessage(ex.getMessage());
        if (printStack) ex.printStackTrace();
        return buildResponseEntity(err);
    }


}
