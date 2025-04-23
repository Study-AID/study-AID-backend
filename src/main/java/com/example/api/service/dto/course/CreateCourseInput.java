package com.example.api.service.dto.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateCourseInput {
    private UUID userId;
    private UUID semesterId;
    private String name;
}
