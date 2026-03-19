package com.wtd.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code=ResultCode.SUCCESS.getCode();
        result.msg=ResultCode.SUCCESS.getMsg();
        result.data=data;
        return result;
    }

    public static <T> Result<T> error(ResultCode resultcode) {
        Result<T> result = new Result<>();
        result.code= resultcode.getCode();
        result.msg= resultcode.getMsg();
        result.data=null;
        return result;
    }
}
