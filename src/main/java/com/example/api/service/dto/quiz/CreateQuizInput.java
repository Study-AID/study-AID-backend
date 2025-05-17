package com.example.api.service.dto.quiz;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.example.api.entity.enums.Status;

@Data
@NoArgsConstructor
public class CreateQuizInput {
    private UUID lectureId;
    private UUID userId;
    private String title;
    private Status status;
}
