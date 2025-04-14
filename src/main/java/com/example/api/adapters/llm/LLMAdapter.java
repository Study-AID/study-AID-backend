package com.example.api.adapters.llm;

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
}
