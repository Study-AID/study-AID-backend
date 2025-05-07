package com.example.api.service;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.entity.Lecture;
import com.example.api.entity.enums.MessageRole;
import com.example.api.exception.InternalServerErrorException;
import com.example.api.exception.NotFoundException;
import com.example.api.exception.UnauthorizedException;
import com.example.api.external.LangchainClient;
import com.example.api.entity.QnaChat;
import com.example.api.entity.QnaChatMessage;
import com.example.api.entity.User;
import com.example.api.external.dto.langchain.MessageContextResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.QnaChatMessageRepository;
import com.example.api.repository.QnaChatRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.qna.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class QnaChatServiceImpl implements QnaChatService {

    private static final Logger log = LoggerFactory.getLogger(QnaChatServiceImpl.class);

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

    // TODO (jin): optimize this logic for better UX and token efficiency
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
        userMsg.setRole(MessageRole.USER);
        userMsg.setContent(input.getQuestion());
        qnaChatMessageRepository.save(userMsg);

        // 강의자료 벡터화 및 출처 검색(Langchain): 질문에 해당하는 강의자료 출처 검색
        List<ReferenceResponse.ReferenceChunkResponse> referenceChunks;
        try {
            // 이미 강의자료가 벡터화 된 후 강의자료 출처 검색
            referenceChunks = langchainClient.findReferences(lectureId, input.getQuestion(), 3).getReferences();
        } catch (NotFoundException e) {
            // 오류 exception
            Lecture lecture = lectureRepository.findById(lectureId)
                    .orElseThrow(() -> new NotFoundException("강의 자료를 찾을 수 없습니다."));
            if (lecture.getParsedText() == null || lecture.getParsedText().isBlank()) {
                log.error("parsedText가 null 또는 공백입니다. 벡터화 요청 중단");
                throw new InternalServerErrorException("강의자료 텍스트가 비어 있어 벡터화할 수 없습니다.");
            }
            // 아직 강의자료가 벡터화 되지 않았다면 벡터화 + 재검색
            langchainClient.vectorizeLecture(lectureId, lecture.getParsedText());
            referenceChunks = langchainClient.findReferences(lectureId, input.getQuestion(), 3).getReferences();
        }
        if (referenceChunks == null) {
            referenceChunks = new ArrayList<>();
        }

        // 텍스트 추출
        List<String> referenceTexts = referenceChunks.stream()
                .map(ReferenceResponse.ReferenceChunkResponse::getText)
                .toList();

        // 대화 맥락 관리(Langchain): 이전에 저장되어있던 대화 맥락 가져오기
        MessageContextResponse responseBefore = langchainClient.getMessageContext(input.getChatId());
        List<ChatMessage> messageContextBefore = responseBefore.getLangchainChatContext();

        // LLM 호출(Spring): 출처, 이전 대화 맥락을 context로 GPT 호출 및 AI 답변 생성
        log.info("[QnaChatService] GPT 호출 시작 - question='{}', referenceCount={}, contextMessageCount={}",
                input.getQuestion(), referenceTexts.size(), messageContextBefore.size());
        String answer;
        try {
            answer = llmAdapter.ask(input.getQuestion(), referenceTexts, messageContextBefore);
        } catch (Exception e) {
            log.error("GPT 호출 오류: {}", e.getMessage(), e);
            return new QnaChatMessageOutput(
                    "assistant",
                    "죄송합니다, GPT 호출 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    messageContextBefore,
                    referenceChunks,
                    Collections.emptyList()
            );
        }
        log.info("[QnaChatService] GPT 응답 수신 완료 - answer length = {}", answer != null ? answer.length() : 0);
        log.debug("[QnaChatService] GPT 응답 내용 일부: {}", answer != null ? answer.substring(0, Math.min(50, answer.length())) : "null");

        // AI 답변 저장
        QnaChatMessage botMsg = new QnaChatMessage();
        botMsg.setQnaChat(chat);
        botMsg.setUser(new User(input.getUserId()));
        botMsg.setRole(MessageRole.ASSISTANT);
        botMsg.setContent(answer);
        qnaChatMessageRepository.save(botMsg);

        // 대화 맥락 관리(Langchain): 사용자 질문, AI 답변 한꺼번에 전달하여 새로운 대화로 맥락에 저장
        List<ChatMessage> newMessages = new ArrayList<>();
        newMessages.add(new ChatMessage("user", input.getQuestion()));
        newMessages.add(new ChatMessage("assistant", answer));

        MessageContextResponse responseAfter = langchainClient.appendMessages(
                input.getChatId(),
                newMessages
        );
        List<ChatMessage> messageContextAfter = responseAfter.getLangchainChatContext();

        if (log.isDebugEnabled()) {
            log.debug("대화 맥락 업데이트 완료: chatId={}, messageCount={}",
                    input.getChatId(), messageContextAfter.size());
        }

        // LLM 호출(Spring): 추천 질문 생성
        List<String> recommendedQuestions = qnaQuestionRecommendService.recommendQuestions(input.getQuestion());

        // 추천 질문까지 포함된 AI 최종 답변 반환
        return new QnaChatMessageOutput(
                "assistant",
                answer,
                messageContextAfter,
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
                .map(m -> new ReadQnaChatOutput.MessageItem(
                        m.getRole().getValue(),
                        m.getContent()
                ))
                .toList();

        return new ReadQnaChatOutput(chat.getId(), messages);
    }
}