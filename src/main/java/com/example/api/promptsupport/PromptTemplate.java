package com.example.api.promptsupport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// spring 에서 동기 호출용
public class PromptTemplate {
    private String model;
    private double temperature;
    private int max_tokens;
    private String system;
    private String user;
}