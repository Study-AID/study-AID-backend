package com.example.api.service.dto.exam;

import com.example.api.entity.EssayCriteriaAnalysis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.example.api.entity.ExamItem;
import com.example.api.entity.ExamResponse;
import com.example.api.entity.enums.QuestionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultElement {
    // elements of Exam Item
    private UUID examItemId;
    private String question;
    private QuestionType questionType;
    private String explanation;
    private Boolean isTrueAnswer;
    private String[] choices;
    private Integer[] answerIndices;
    private String textAnswer;
    private Float points;

    // elements of Exam Response
    private UUID examResponseId;
    private Boolean isCorrect;
    private Boolean selectedBool;
    private Integer[] selectedIndices;
    private String textAnswerOfUser;
    private EssayCriteriaAnalysis essayCriteriaAnalysis;
    private Float score;

    public static ExamResultElement fromExamItemAndResponse(ExamItem examItem, ExamResponse examResponse) {
        ExamResultElement element = new ExamResultElement();
        element.setExamItemId(examItem.getId());
        element.setQuestion(examItem.getQuestion());
        element.setQuestionType(examItem.getQuestionType());
        element.setExplanation(examItem.getExplanation());
        element.setIsTrueAnswer(examItem.getIsTrueAnswer());

        element.setExamResponseId(examResponse.getId());
        element.setIsCorrect(examResponse.getIsCorrect());
        element.setEssayCriteriaAnalysis(examResponse.getEssayCriteriaAnalysis());

        if (examItem.getQuestionType() == QuestionType.true_or_false) {
            element.setChoices(examItem.getChoices());
            element.setSelectedBool(examResponse.getSelectedBool());
        } else if (examItem.getQuestionType() == QuestionType.multiple_choice) {
            element.setChoices(examItem.getChoices());
            element.setSelectedIndices(examResponse.getSelectedIndices());
        } else if (examItem.getQuestionType() == QuestionType.short_answer || examItem.getQuestionType() == QuestionType.essay) {
            element.setTextAnswer(examItem.getTextAnswer());
            element.setTextAnswerOfUser(examResponse.getTextAnswer());
            element.setEssayCriteriaAnalysis(examResponse.getEssayCriteriaAnalysis());
        }

        return element;
    }
}
