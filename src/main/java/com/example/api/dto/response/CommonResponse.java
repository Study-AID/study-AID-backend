package com.example.api.dto.response;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private String message;
    private T data;
}