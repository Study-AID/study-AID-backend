package com.example.api.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message);
    }
}
