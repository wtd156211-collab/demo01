package com.wtd.exception;

import com.wtd.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        //算数异常
                    if (e instanceof ArithmeticException) {
            return new Result("500", "算数异常");
        }
        return new Result("500", "服务器异常：" + e.getMessage());
    }
}