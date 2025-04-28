package com.example.api.controller.dto.semester;

import com.example.api.service.dto.semester.SemesterOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of semesters response")
public class SemesterListResponse {
    @Schema(description = "List of semesters")
    private List<SemesterResponse> semesters;

    public static SemesterListResponse fromServiceDto(List<SemesterOutput> semesters) {
        List<SemesterResponse> semesterResponses = semesters.stream()
                .map(SemesterResponse::fromServiceDto)
                .collect(Collectors.toList());
        return new SemesterListResponse(semesterResponses);
    }
}