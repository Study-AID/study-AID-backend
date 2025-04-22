package com.example.api.exception.auth;

public class WrongLoginInputException extends RuntimeException {
    public WrongLoginInputException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}