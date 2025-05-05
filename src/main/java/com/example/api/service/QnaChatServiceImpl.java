package com.example.api.service;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.entity.Lecture;
import com.example.api.exception.NotFoundException;
import com.example.api.exception.UnauthorizedException;
import com.example.api.external.LangchainClient;
import com.example.api.entity.QnaChat;
import com.example.api.entity.QnaChatMessage;
import com.example.api.entity.User;
import com.example.api.external.dto.langchain.MessageHistoryResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.QnaChatMessageRepository;
import com.example.api.repository.QnaChatRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.qna.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class QnaChatServiceImpl implements QnaChatService {

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final QnaChatRepository qnaChatRepository;
    private final QnaChatMessageRepository qnaChatMessageRepository;
    private final LangchainClient langchainClient;
    private final QnaQuestionRecommendService qnaQuestionRecommendService;
    private final LLMAdapter llmAdapter;

    @Override
    public CreateQnaChatOutput createQnaChat(CreateQnaChatInput input) {
        User user = userRepository.findById(input.getUserId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));
        Lecture lecture = lectureRepository.findById(input.getLectureId())
                .orElseThrow(() -> new NotFoundException("강의 자료를 찾을 수 없습니다"));

        QnaChat chat = new QnaChat();
        chat.setUser(user);
        chat.setLecture(lecture);

        chat = qnaChatRepository.save(chat);
        return new CreateQnaChatOutput(chat.getId());
    }

    @Override
    public QnaChatMessageOutput ask(QnaChatMessageInput input) {
        QnaChat chat = qnaChatRepository.findById(input.getChatId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다"));
        if (!chat.getUser().getId().equals(input.getUserId())) {
            throw new UnauthorizedException("해당 채팅방에 대한 접근 권한이 없습니다");
        }

        UUID lectureId = chat.getLecture().getId();

        // 사용자 질문 DB에 저장
        QnaChatMessage userMsg = new QnaChatMessage();
        userMsg.setQnaChat(chat);
        userMsg.setUser(new User(input.getUserId()));
        userMsg.setQuestion(input.getQuestion());
        qnaChatMessageRepository.save(userMsg);

        // 출처 검색(Langchain): 질문에 해당하는 강의자료 출처 검색
        ReferenceResponse referenceResponse = langchainClient.findReferences(lectureId, input.getQuestion(), 3);
        List<ReferenceResponse.ReferenceChunkResponse> referenceChunks = referenceResponse.getReferences();
        if (referenceChunks == null) {
            referenceChunks = new ArrayList<>();
        }

        // 텍스트 추출
        List<String> referenceTexts = referenceChunks.stream()
                .map(ReferenceResponse.ReferenceChunkResponse::getText)
                .toList();

        // 대화 맥락 관리(Langchain): 이전에 저장되어있던 대화 맥락 가져오기
        MessageHistoryResponse responseBefore = langchainClient.getMessageHistory(input.getChatId());
        List<ChatMessage> messageHistoryBefore = responseBefore.getLangchainChatHistory();

        // LLM 호출(Spring): 출처, 이전 대화 맥락을 context로 GPT 호출 및 AI 답변 생성
        String answer = llmAdapter.ask(input.getQuestion(), referenceTexts, messageHistoryBefore);

        // AI 답변 저장
        QnaChatMessage botMsg = new QnaChatMessage();
        botMsg.setQnaChat(chat);
        botMsg.setUser(null);
        botMsg.setAnswer(answer);
        qnaChatMessageRepository.save(botMsg);

        // 대화 맥락 관리(Langchain): 사용자 질문, AI 답변 한꺼번에 전달하여 새로운 대화로 맥락에 저장
        MessageHistoryResponse responseAfter = langchainClient.appendMessage(
                input.getChatId(),
                input.getQuestion(),
                answer
        );
        List<ChatMessage> messageHistoryAfter = responseAfter.getLangchainChatHistory();

        // LLM 호출(Spring): 추천 질문 생성
        List<String> recommendedQuestions = qnaQuestionRecommendService.recommendQuestions(input.getQuestion());

        // 추천 질문까지 포함된 AI 최종 답변 반환
        return new QnaChatMessageOutput(
                input.getQuestion(),
                answer,
                messageHistoryAfter,
                referenceChunks,
                recommendedQuestions
        );
    }

    @Override
    public ReadQnaChatOutput readQnaChat(ReadQnaChatInput input) {
        QnaChat chat = qnaChatRepository.findById(input.getChatId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다"));
        if (!chat.getUser().getId().equals(input.getUserId())) {
            throw new UnauthorizedException("해당 채팅방에 대한 접근 권한이 없습니다");
        }

        List<ReadQnaChatOutput.MessageItem> messages = qnaChatMessageRepository.findByQnaChatId(chat.getId()).stream()
                .map(m -> new ReadQnaChatOutput.MessageItem(m.getQuestion(), m.getAnswer()))
                .toList();

        return new ReadQnaChatOutput(messages);
    }
}