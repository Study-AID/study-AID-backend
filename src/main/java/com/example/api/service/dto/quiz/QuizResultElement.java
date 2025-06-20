package com.example.api.service.dto.quiz;

import com.example.api.entity.EssayCriteriaAnalysis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.example.api.entity.QuizItem;
import com.example.api.entity.QuizResponse;
import com.example.api.entity.enums.QuestionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultElement {
    // elements of Quiz Item
    private UUID quizItemId;
    private String question;
    private QuestionType questionType;
    private String explanation;
    private Boolean isTrueAnswer;
    private String[] choices;
    private Integer[] answerIndices;
    private String textAnswer;
    private Float points;

    // elements of Quiz Response
    private UUID quizResponseId;
    private Boolean isCorrect;
    private Boolean selectedBool;
    private Integer[] selectedIndices;
    private String textAnswerOfUser;
    private EssayCriteriaAnalysis essayCriteriaAnalysis;
    private Float score;

    public static QuizResultElement fromQuizItemAndResponse(QuizItem quizItem, QuizResponse quizResponse) {
        QuizResultElement element = new QuizResultElement();
        element.setQuizItemId(quizItem.getId());
        element.setQuestion(quizItem.getQuestion());
        element.setQuestionType(quizItem.getQuestionType());
        element.setExplanation(quizItem.getExplanation());
        element.setPoints(quizItem.getPoints());

        element.setQuizResponseId(quizResponse.getId());
        element.setIsCorrect(quizResponse.getIsCorrect());
        element.setScore(quizResponse.getScore());
        
        if (quizItem.getQuestionType() == QuestionType.true_or_false) {
            element.setIsTrueAnswer(quizItem.getIsTrueAnswer());
            element.setSelectedBool(quizResponse.getSelectedBool());
        } else if (quizItem.getQuestionType() == QuestionType.multiple_choice) {
            element.setChoices(quizItem.getChoices());
            element.setAnswerIndices(quizItem.getAnswerIndices());
            element.setSelectedIndices(quizResponse.getSelectedIndices());
        } else if (quizItem.getQuestionType() == QuestionType.short_answer) {
            element.setTextAnswer(quizItem.getTextAnswer());
            element.setTextAnswerOfUser(quizResponse.getTextAnswer());
        } else if (quizItem.getQuestionType() == QuestionType.essay) {
            element.setTextAnswer(quizItem.getTextAnswer());
            element.setTextAnswerOfUser(quizResponse.getTextAnswer());
            element.setEssayCriteriaAnalysis(quizResponse.getEssayCriteriaAnalysis());
        }

        return element;
    }
}
