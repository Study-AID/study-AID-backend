package com.example.api.exception.auth;

public class RefreshTokenMismatchException extends RuntimeException {
    public RefreshTokenMismatchException() {
        super("리프레시 토큰이 일치하지 않습니다.");
    }
}