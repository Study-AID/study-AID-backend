package com.example.api.service.dto.quiz;

import java.util.List;

import com.example.api.entity.QuizItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizItemListOutput {
    private List<QuizItemOutput> quizItems;

    public static QuizItemListOutput fromEntities(List<QuizItem> quizItems) {
        List<QuizItemOutput> quizItemOutputs = quizItems.stream()
                .map(QuizItemOutput::fromEntity)
                .toList();
        return new QuizItemListOutput(quizItemOutputs);
    }
}