package com.example.api.exception.auth;

public class InvalidAccessTokenException extends RuntimeException {
    public InvalidAccessTokenException() {
        super("유효하지 않은 액세스 토큰입니다.");
    }
}