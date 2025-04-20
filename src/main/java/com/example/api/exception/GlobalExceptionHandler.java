package com.example.api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.api.dto.response.CommonResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<Void>> handleRuntime(RuntimeException e) {
        return ResponseEntity.badRequest().body(new CommonResponse<>(e.getMessage(), null));
    }
}