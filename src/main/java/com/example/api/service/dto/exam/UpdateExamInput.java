package com.example.api.service.dto.exam;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateExamInput {
    
    private UUID id;
    private String title;
}
