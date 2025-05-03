package com.example.api.external.dto.rag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RagAnswer {
    private String answer;
    private String source;
}
