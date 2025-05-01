package com.example.api.service.dto.semester;

import com.example.api.entity.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterListOutput {
    private List<SemesterOutput> semesters;

    public static SemesterListOutput fromEntities(List<Semester> semesters) {
        List<SemesterOutput> semesterOutputs = semesters.stream()
                .map(SemesterOutput::fromEntity)
                .collect(Collectors.toList());
        return new SemesterListOutput(semesterOutputs);
    }
}