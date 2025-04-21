package com.example.api.service.dto.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateCourseInput {
    private UUID id;
    private String name;
}
