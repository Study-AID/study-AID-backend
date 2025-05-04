package com.example.api.service;

import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.promptsupport.PromptLoader;
import com.example.api.promptsupport.PromptPaths;
import com.example.api.promptsupport.PromptTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaQuestionRecommendServiceImpl implements QnaQuestionRecommendService {

    private final LLMAdapter llmAdapter;

    @Override
    public List<String> recommendQuestions(String userQuestion) {
        PromptTemplate template = PromptLoader.load(PromptPaths.QNA_RECOMMEND_QUESTIONS_V1);
        String prompt = template.getUser().replace("{{question}}", userQuestion);

        String response = llmAdapter.complete(prompt);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.singletonList("추천 질문을 생성하지 못했습니다.");
        }
    }
}

