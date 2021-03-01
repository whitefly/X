package com.entity;

import lombok.Data;

@Data
public class ResponseVO {
    Integer code;
    String msg;
    Object rnt;

    public ResponseVO() {
        code = 0;
        msg = "ok";
    }

    public ResponseVO(Object rnt) {
        this();
        this.rnt = rnt;
    }

    public ResponseVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
