package com.example.api.service.dto.semester;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSemesterDatesInput {
    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
}