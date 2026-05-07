package com.wtd.exception;

import com.wtd.common.Result;
import com.wtd.common.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("接口异常", e);

        if (e instanceof MethodArgumentNotValidException || e instanceof ConstraintViolationException || e instanceof IllegalArgumentException) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        if (e instanceof IllegalStateException) {
            return new Result<>(500, e.getMessage(), null);
        }

        //算数异常
        if (e instanceof ArithmeticException) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        //其他异常
        return Result.error(ResultCode.SERVER_ERROR);
    }
}
