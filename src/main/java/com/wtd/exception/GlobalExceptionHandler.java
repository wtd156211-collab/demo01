package com.wtd.exception;

import com.wtd.common.Result;
import com.wtd.common.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        //算数异常
        if (e instanceof ArithmeticException) {
            return Result.error(ResultCode.PARAM_ERROR);
        }
        return Result.error(ResultCode.SERVER_ERROR);
    }
}