package com.example.api.external.dto.langchain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReferenceRequest {
    private String question;
    private int k;
}