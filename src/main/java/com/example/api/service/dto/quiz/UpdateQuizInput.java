package com.example.api.service.dto.quiz;

import java.util.UUID;

import com.example.api.entity.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateQuizInput {
    private UUID id;
    private String title;
    private Status status;
}
