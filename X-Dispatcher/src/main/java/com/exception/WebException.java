package com.exception;

import lombok.Getter;

@Getter
public class WebException extends RuntimeException {
    ErrorCode errorCode;

    public WebException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
