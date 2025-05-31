package com.example.api.service.dto.exam;


import java.time.LocalDateTime;

import com.example.api.entity.ExamItem;
import com.example.api.entity.enums.QuestionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamItemOutput {
    private String id;
    private String examId;
    private String userId;
    private String question;
    private QuestionType questionType;
    private String explanation;
    private Boolean isTrueAnswer;
    private String[] choices;
    private Integer[] answerIndices;
    private String textAnswer;
    private Integer displayOrder;
    private Float points;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;   

    public static ExamItemOutput fromEntity(ExamItem examItem) {
        return new ExamItemOutput(
                examItem.getId().toString(),
                examItem.getExam().getId().toString(),
                examItem.getUser().getId().toString(),
                examItem.getQuestion(),
                examItem.getQuestionType(),
                examItem.getExplanation(),
                examItem.getIsTrueAnswer(),
                examItem.getChoices(),
                examItem.getAnswerIndices(),
                examItem.getTextAnswer(),
                examItem.getDisplayOrder(),
                examItem.getPoints(),
                examItem.getIsLiked(),
                examItem.getCreatedAt(),
                examItem.getUpdatedAt()
        );
    }
}
