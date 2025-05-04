package com.example.api.adapters.llm;

import java.util.List;

/**
 * LLMAdapter는 다양한 LLM 서비스를 사용하기 위한 공통 인터페이스입니다.
 */
public interface LLMAdapter {
    /**
     * LLM에 텍스트 기반 프롬프트를 전송하고 응답을 받습니다.
     *
     * @param prompt LLM에 전송할 프롬프트 텍스트
     * @return LLM의 응답 텍스트
     */
    String complete(String prompt);

    /**
     * LLM의 채팅 API를 사용하여 대화를 수행합니다.
     *
     * @param messages 대화 메시지 리스트
     * @return LLM의 응답 텍스트
     */
    String chat(ChatMessage[] messages);

    /**
     * LLM 프로바이더 이름 반환.
     */
    String getProviderName();

    /**
     * LLM에 context가 포함된 질문을 하고 답변을 받습니다.
     *
     * @param question 질문 텍스트
     * @param referenceChunks 출처 청크 리스트
     * @param messageHistory  대화 문맥 리스트
     * @return LLM의 응답 텍스트
     */
    String ask(String question, List<String> referenceChunks, List<ChatMessage> messageHistory);
}
