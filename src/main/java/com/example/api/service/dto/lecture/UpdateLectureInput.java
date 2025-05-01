package com.example.api.service.dto.lecture;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateLectureInput {
    private UUID id;
    private String title;
    private String materialPath;
    private String materialType;
}
