package com.example.api.exception.auth;

import com.example.api.controller.AuthController;
import com.example.api.dto.response.SimpleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<SimpleResponse<Void>> handleEmailExists(EmailAlreadyExistsException e) {
        return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
    }

    @ExceptionHandler(WrongLoginInputException.class)
    public ResponseEntity<SimpleResponse<Void>> handleWrongLoginInput(WrongLoginInputException e) {
        return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
    }

    @ExceptionHandler(WrongAuthTypeException.class)
    public ResponseEntity<SimpleResponse<Void>> handleWrongAuthType(WrongAuthTypeException e) {
        return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<SimpleResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        return ResponseEntity.status(401).body(new SimpleResponse<>(e.getMessage(), null));
    }

    @ExceptionHandler(RefreshTokenMismatchException.class)
    public ResponseEntity<SimpleResponse<Void>> handleRefreshTokenMismatch(RefreshTokenMismatchException e) {
        return ResponseEntity.status(401).body(new SimpleResponse<>(e.getMessage(), null));
    }

    @ExceptionHandler(InvalidAccessTokenException.class)
    public ResponseEntity<SimpleResponse<Void>> handleInvalidAccessToken(InvalidAccessTokenException e) {
        return ResponseEntity.status(401).body(new SimpleResponse<>(e.getMessage(), null));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<SimpleResponse<Void>> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(404).body(new SimpleResponse<>(e.getMessage(), null));
    }
}
