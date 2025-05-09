package com.example.api.service;

import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.promptsupport.PromptLoader;
import com.example.api.promptsupport.PromptPaths;
import com.example.api.promptsupport.PromptTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaQuestionRecommendServiceImpl implements QnaQuestionRecommendService {

    private final LLMAdapter llmAdapter;
    private static final Logger log = LoggerFactory.getLogger(QnaQuestionRecommendServiceImpl.class);

    @Override
    public List<String> recommendQuestions(String userQuestion) {
        PromptTemplate template = PromptLoader.load(PromptPaths.QNA_RECOMMEND_QUESTIONS_V1);
        String prompt = template.getUser().replace("{{question}}", userQuestion);

        String response = llmAdapter.complete(prompt);

        try {
            log.debug("추천 질문 OpenAI 원본 응답: {}", response);

            String jsonArrayStr = extractJsonArray(response);
            log.debug("추출된 JSON 배열: {}", jsonArrayStr);

            if (jsonArrayStr.isEmpty()) {
                log.warn("JSON 배열을 찾을 수 없습니다. 원본 응답: {}", response);
                return getFallbackQuestions();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            List<String> questions = objectMapper.readValue(jsonArrayStr, new TypeReference<List<String>>() {});

            if (questions == null || questions.isEmpty()) {
                log.warn("파싱된 질문 목록이 비어 있습니다.");
                return getFallbackQuestions();
            }

            return questions;
        } catch (Exception e) {
            log.error("추천 질문 파싱 오류: {}", e.getMessage(), e);
            return getFallbackQuestions();
        }
    }

    private String extractJsonArray(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int startIdx = text.indexOf('[');
        int endIdx = text.lastIndexOf(']');

        if (startIdx >= 0 && endIdx > startIdx) {
            return text.substring(startIdx, endIdx + 1);
        }

        return "";
    }

    // 기본 질문 입니다.
    private List<String> getFallbackQuestions() {
        return List.of(
                "이 개념의 실제 활용 예시는 무엇인가요?",
                "이와 관련된 다른 개념은 무엇이 있나요?",
                "이 주제에 대해 더 자세히 알고 싶은 부분이 있나요?"
        );
    }
}

