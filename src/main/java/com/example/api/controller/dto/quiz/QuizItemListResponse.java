package com.example.api.controller.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.entity.QuizItem;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of quizz items response")
public class QuizItemListResponse {
    @Schema(description = "List of quiz items")
    private List<QuizItemResponse> quizItems;

    public static QuizItemListResponse fromEntities(List<QuizItem> quizItems) {
        return new QuizItemListResponse(
                quizItems.stream()
                        .map(QuizItemResponse::fromEntity)
                        .toList()
        );
    }
}
