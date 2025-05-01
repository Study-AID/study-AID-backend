package com.example.api.service.dto.semester;

import com.example.api.entity.enums.Season;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSemesterInput {
    private UUID userId;
    private String name;
    private int year;
    private Season season;
}