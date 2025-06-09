package com.example.api.exception.auth;

public class WrongAuthTypeException extends RuntimeException {
    public WrongAuthTypeException() {
        super("해당 로그인 방식 사용자가 아닙니다. 다른 로그인 방식으로 시도해보세요.");
    }
}