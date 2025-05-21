package com.example.api.service;

import java.util.List;

public interface QnaQuestionRecommendService {
    List<String> recommendQuestions(String userQuestion);
}
