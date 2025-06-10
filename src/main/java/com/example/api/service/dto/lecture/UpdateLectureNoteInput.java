package com.example.api.service.dto.lecture;

import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateLectureNoteInput {
    private UUID id;
    private Map<String, Object> note;
}
