package com.example.api.service.dto.courseAssessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.entity.CourseAssessment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseAssessmentListOutput {
    private List<CourseAssessmentOutput> courseAssessments;

    public static CourseAssessmentListOutput fromEntities(List<CourseAssessment> courseAssessments) {
        List<CourseAssessmentOutput> courseAssessmentOutputs = courseAssessments.stream()
                .map(CourseAssessmentOutput::fromEntity)
                .toList();
        return new CourseAssessmentListOutput(courseAssessmentOutputs);
    }
}