package com.example.api.controller.dto.quiz;

import java.util.List;

import com.example.api.service.dto.quiz.QuizResponseListOutput;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitQuizListResponse {
    List<SubmitQuizResponse> submitQuizResponses;

    public static SubmitQuizListResponse fromServiceDto(QuizResponseListOutput quizResponseListOutput) {
        return new SubmitQuizListResponse(
            quizResponseListOutput.getQuizResponseOutputs().stream()
                        .map(SubmitQuizResponse::fromServiceDto)
                        .toList()
        );
    }
}
