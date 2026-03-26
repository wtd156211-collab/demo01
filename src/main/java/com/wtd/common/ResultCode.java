package com.wtd.common;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统繁忙，请稍后再试"),
    TOKEN_INVALID(401, "token验证失败"),
    //参数异常
    PARAM_ERROR(400, "参数异常"),
    //服务端异常
    SERVER_ERROR(500, "服务端异常"),
    USER_HAS_EXISTED(4001, "用户已存在"),
    USER_NOT_EXIST(4002, "用户不存在"),
    PASSWORD_ERROR(4003, "账号或密码错误");

    private final Integer code;
    private final String msg;

    ResultCode(Integer number, String msg) {
        this.code = number;
        this.msg = msg;
    }
    public Integer getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}

