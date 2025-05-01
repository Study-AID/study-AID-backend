package com.example.api.service.dto.lecture;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateLectureDisplayOrderLexInput {
    private UUID id;
    private String displayOrderLex;
}
