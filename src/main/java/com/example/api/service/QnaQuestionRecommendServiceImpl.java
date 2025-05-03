package com.example.api.service;

import com.example.api.adapters.llm.LLMAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaQuestionRecommendServiceImpl implements QnaQuestionRecommendService {

    private final LLMAdapter llmAdapter;

    @Override
    public List<String> recommendQuestions(String userQuestion) {
        String prompt = String.format(
            """
            '%s'라는 질문을 한 사용자에게, 연관된 추가 질문 3가지를 추천해줘. 
            각 질문은 한 줄씩, 리스트 형식으로 작성해줘.
            """, userQuestion);

        String response = llmAdapter.complete(prompt);

        return Arrays.stream(response.split("\n"))
                .map(line -> line.replaceAll("^[\\-\\d\\.\\s]+", "").trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }
}

