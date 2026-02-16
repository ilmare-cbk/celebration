package com.celebration.service;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final ErrorCode code;
    private final HttpStatus httpStatus;

    public AppException(ErrorCode code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public ErrorCode getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
