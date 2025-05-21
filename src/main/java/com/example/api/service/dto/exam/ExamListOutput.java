package com.example.api.service.dto.exam;

import java.util.List;
import java.util.stream.Collectors;

import com.example.api.entity.Exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamListOutput {
    private List<ExamOutput> exams;

    public static ExamListOutput fromEntities(List<Exam> exams) {
        List<ExamOutput> examOutputs = exams.stream()
                .map(exam -> new ExamOutput(
                        exam.getId(),
                        exam.getCourse().getId(),
                        exam.getUser().getId(),
                        exam.getTitle(),
                        exam.getStatus(),
                        exam.getReferencedLectures(),
                        exam.getContentsGenerateAt(),
                        exam.getCreatedAt(),
                        exam.getUpdatedAt(),
                        null
                ))
                .collect(Collectors.toList());
        return new ExamListOutput(examOutputs);
    }
}
