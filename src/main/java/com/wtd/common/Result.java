package com.wtd.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result {
    private String code;
    private String msg;
    private Object data;

    public Result(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public Result success() {
        return new Result("0", "success", null);
    }
    public Result error(Object data) {
        return new Result("1", "error", data);
    }
}
