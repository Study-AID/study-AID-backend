package com.example.api.service.dto.quiz;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateQuizResponseListInput {
    private List<CreateQuizResponseInput> quizResponseInputs;

    public CreateQuizResponseListInput(List<CreateQuizResponseInput> quizResponseInputs) {
        this.quizResponseInputs = quizResponseInputs;
    }
}
