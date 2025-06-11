package com.example.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sqs")
@Getter
@Setter
public class SQSMessageConfig {
    private GenerateSummary generateSummary = new GenerateSummary();
    private GenerateQuiz generateQuiz = new GenerateQuiz();
    private GenerateExam generateExam = new GenerateExam();
    private GradeQuizEssay gradeQuizEssay = new GradeQuizEssay();
    private GradeExamEssay gradeExamEssay = new GradeExamEssay();
    private GenerateCourseWeaknessAnalysis generateCourseWeaknessAnalysis = new GenerateCourseWeaknessAnalysis();

    @Getter
    @Setter
    public static class GenerateSummary {
        private String queueUrl;
    }

    @Getter
    @Setter
    public static class GenerateQuiz {
        private String queueUrl;
    }

    @Getter
    @Setter
    public static class GenerateExam {
        private String queueUrl;
    }

    @Getter
    @Setter
    public static class GradeQuizEssay {
        private String queueUrl;
    }

    @Getter
    @Setter
    public static class GradeExamEssay {
        private String queueUrl;
    }

    @Getter
    @Setter
    public static class GenerateCourseWeaknessAnalysis {
        private String queueUrl;
    }
}
