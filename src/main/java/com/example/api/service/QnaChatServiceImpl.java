package com.example.api.service;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.entity.Lecture;
import com.example.api.entity.ParsedText;
import com.example.api.entity.enums.MessageRole;
import com.example.api.exception.BadRequestException;
import com.example.api.exception.InternalServerErrorException;
import com.example.api.exception.NotFoundException;
import com.example.api.external.LangchainClient;
import com.example.api.entity.QnaChat;
import com.example.api.entity.QnaChatMessage;
import com.example.api.entity.User;
import com.example.api.external.dto.langchain.EmbeddingCheckResponse;
import com.example.api.external.dto.langchain.MessageContextResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.repository.*;
import com.example.api.service.dto.qna.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
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

    private static final String USER = "user";
    private static final String ASSISTANT = "assistant";

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final QnaChatRepository qnaChatRepository;
    private final QnaChatMessageRepository qnaChatMessageRepository;
    private final LangchainClient langchainClient;
    private final QnaQuestionRecommendService qnaQuestionRecommendService;
    private final LLMAdapter llmAdapter;

    @Override
    @Transactional
    public CreateQnaChatOutput createQnaChat(CreateQnaChatInput input) {
        User user = new User(input.getUserId());
        Lecture lecture = lectureRepository.findById(input.getLectureId())
                .orElseThrow(() -> new NotFoundException("강의 자료를 찾을 수 없습니다"));

        // 이미 존재하는 채팅방이 있는지 확인 및 있으면 원래 채팅방 반환
        QnaChat existingChat = qnaChatRepository.findByLectureIdAndUserId(input.getLectureId(), input.getUserId())
                .orElse(null);
        if (existingChat != null) {
            log.info("강의 {} - 사용자 {} 조합으로 이미 생성된 채팅방이 존재합니다. 기존 채팅방 ID: {}",
                    input.getLectureId(), input.getUserId(), existingChat.getId());
            return new CreateQnaChatOutput(existingChat.getId(), existingChat.getCreatedAt());
        }

        ParsedText parsedText = lecture.getParsedText();

        // 강의자료 벡터화(Langchain): 채팅방 생성 시 수행
        log.info("강의 자료 {} 벡터화 여부 LangChain 서버에서 확인 중...", lecture.getId());
        EmbeddingCheckResponse checkResponse = langchainClient.checkEmbeddingStatus(lecture.getId());

        if (checkResponse != null && checkResponse.isVectorized()) {
            log.info("강의 자료 {} 벡터화 상태 정상", lecture.getId());
        } else {
            log.info("강의 자료 {} 벡터화 필요: 상태 비정상", lecture.getId());
            if (parsedText == null || parsedText.getPages() == null || parsedText.getPages().isEmpty()) {
                throw new BadRequestException("강의 자료에 텍스트가 없습니다: " + lecture.getId());
            }
            try {
                langchainClient.generateLectureEmbeddings(lecture.getId(), parsedText);
                lecture.setIsVectorized(true);
                lectureRepository.save(lecture);
                log.info("강의 자료 {} 벡터화 성공 및 is_vectorized 반영 완료", lecture.getId());
            } catch (Exception ex) {
                log.warn("강의 자료 {} 벡터화 실패. 채팅방은 생성되나, 첫 질문 시 벡터화가 재시도됩니다.", lecture.getId(), ex);
            }
        }

        QnaChat chat = new QnaChat();
        chat.setUser(user);
        chat.setLecture(lecture);

        chat = qnaChatRepository.save(chat);
        return new CreateQnaChatOutput(chat.getId(), chat.getCreatedAt());
    }

    @Override
    public GetQnaChatIdOutput getQnaChatId(GetQnaChatIdInput input) {
        User user = userRepository.findById(input.getUserId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));
        QnaChat chat = qnaChatRepository.findByLectureIdAndUserId(input.getLectureId(), input.getUserId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다"));

        // 벡터화 상태 확인 필드 업데이트
        Lecture lecture = chat.getLecture();
        boolean isVectorized = lecture.getIsVectorized();

        return new GetQnaChatIdOutput(chat.getId(), isVectorized);
    }

    @Override
    public QnaChatMessageOutput ask(QnaChatMessageInput input) {
        long startTime = System.currentTimeMillis();
        log.info("[QnaChatService] ask 메소드 시작: lectureId={}, userId={}", input.getLectureId(), input.getUserId());

        User user = new User(input.getUserId());

        QnaChat chat = qnaChatRepository.findByLectureIdAndUserId(input.getLectureId(), input.getUserId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다"));
        UUID lectureId = chat.getLecture().getId();
        log.info("[Performance] 채팅방 조회 완료: 소요시간={}ms", System.currentTimeMillis() - startTime);

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new NotFoundException("강의 자료를 찾을 수 없습니다."));
        ParsedText parsedText = lecture.getParsedText();

        // 강의자료 벡터화 확인 및 재시도(Langchain)
        log.info("[QnaChatService] LangChain에서 벡터 스토어 상태 확인 시작: lectureId={}", lectureId);
        EmbeddingCheckResponse checkResponse = langchainClient.checkEmbeddingStatus(lectureId);
        if (checkResponse == null || !checkResponse.isVectorized()) {
            log.info("[QnaChatService] 강의자료 벡터화 필요 또는 손상. 벡터화 재시도 합니다.: lectureId={}", lectureId);
            if (parsedText == null || parsedText.getPages() == null || parsedText.getPages().isEmpty()) {
                log.error("parsedText가 null 또는 공백입니다. 벡터화 요청 중단");
                throw new BadRequestException("강의 자료에 텍스트가 없습니다: " + lectureId); 
            }
            try {
                long vectorizationStartTime = System.currentTimeMillis();
                langchainClient.generateLectureEmbeddings(lectureId, parsedText);
                lecture.setIsVectorized(true); 
                lectureRepository.save(lecture);
                log.info("[Performance] 강의자료 벡터화 재시도 성공: lectureId={}, 소요시간={}ms", lectureId, System.currentTimeMillis() - vectorizationStartTime);
            } catch (Exception e) {
                log.error("강의자료 벡터화 재시도 실패", e);
                throw new InternalServerErrorException("강의자료 분석 중입니다. 잠시 후 다시 시도해주세요."); 
            }
        } else {
            log.info("[QnaChatService] 강의자료 정상 벡터화 완료: lectureId={}", lectureId);
        }

        // 사용자 질문 DB에 저장
        long saveQuestionStartTime = System.currentTimeMillis();
        QnaChatMessage userMsg = new QnaChatMessage();
        userMsg.setQnaChat(chat);
        userMsg.setUser(user);
        userMsg.setRole(MessageRole.USER);
        userMsg.setContent(input.getQuestion());
        userMsg = qnaChatMessageRepository.save(userMsg);
        log.info("[Performance] 사용자 질문 DB에 저장 완료: 소요시간={}ms", System.currentTimeMillis() - saveQuestionStartTime);

        // 강의자료 출처 검색(Langchain): 질문에 해당하는 강의자료 출처 검색
        long referenceSearchStartTime = System.currentTimeMillis();
        List<ReferenceResponse.ReferenceChunkResponse> referenceChunks;
        log.info("[QnaChatService] 강의자료 레퍼런스 검색 시작: question='{}', lectureId={}", input.getQuestion(), lectureId);
        referenceChunks = langchainClient.findReferencesInLecture(lectureId, input.getQuestion(), 3, 0.3).getReferences();
        if (referenceChunks == null) {
            referenceChunks = new ArrayList<>();
        }
        log.info("[Performance] 강의자료 레퍼런스 검색 완료: 검색된 청크 수={}, 소요시간={}ms", referenceChunks.size(), System.currentTimeMillis() - referenceSearchStartTime);
        List<String> referenceTexts = referenceChunks.stream()
                .map(ReferenceResponse.ReferenceChunkResponse::getText)
                .toList();

        // 대화 맥락 관리(Langchain): 이전에 저장되어있던 대화 맥락 가져오기
        long contextFetchStartTime = System.currentTimeMillis();
        MessageContextResponse responseBefore = langchainClient.getMessageContext(chat.getId());
        List<ChatMessage> messageContextBefore = responseBefore.getLangchainChatContext();
        log.info("[Performance] 대화 맥락 가져오기 완료: 메시지 수={}, 소요시간={}ms", messageContextBefore.size(), System.currentTimeMillis() - contextFetchStartTime);

        // LLM 호출(Spring): 출처, 이전 대화 맥락을 context로 GPT 호출 및 AI 답변 생성
        log.info("[QnaChatService] GPT 호출 시작 - question='{}', referenceCount={}, contextMessageCount={}",
                input.getQuestion(), referenceTexts.size(), messageContextBefore.size());
        String answer;
        long llmCallStartTime = System.currentTimeMillis();
        try {
            answer = llmAdapter.ask(input.getQuestion(), referenceTexts, messageContextBefore);
            log.info("[Performance] GPT 호출 및 답변 생성 완료: 응답 길이={}, 소요시간={}ms", answer != null ? answer.length() : 0, System.currentTimeMillis() - llmCallStartTime);
        } catch (Exception e) {
            log.error("[Performance] GPT 호출 오류: 소요시간={}ms, 오류={}", System.currentTimeMillis() - llmCallStartTime, e.getMessage(), e);
            return new QnaChatMessageOutput(
                    UUID.randomUUID(),
                    ASSISTANT,
                    "죄송합니다, GPT 호출 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    messageContextBefore,
                    referenceChunks,
                    Collections.emptyList(),
                    java.time.LocalDateTime.now(),
                    false
            );
        }
        log.debug("[QnaChatService] GPT 응답 내용 일부: {}", answer != null ? answer.substring(0, Math.min(50, answer.length())) : "null");

        // AI 답변 DB에 저장
        long saveAnswerStartTime = System.currentTimeMillis();
        QnaChatMessage botMsg = new QnaChatMessage();
        botMsg.setQnaChat(chat);
        botMsg.setUser(user);
        botMsg.setRole(MessageRole.ASSISTANT);
        botMsg.setContent(answer);
        botMsg.setReferences(referenceChunks);
        botMsg = qnaChatMessageRepository.save(botMsg);
        log.info("[Performance] AI 답변 DB에 저장 완료: 소요시간={}ms", System.currentTimeMillis() - saveAnswerStartTime);

        // 대화 맥락 관리(Langchain): 사용자 질문, AI 답변 한꺼번에 전달하여 새로운 대화로 맥락에 저장
        long contextUpdateStartTime = System.currentTimeMillis();
        List<ChatMessage> newMessages = new ArrayList<>();
        newMessages.add(new ChatMessage(USER, input.getQuestion()));
        newMessages.add(new ChatMessage(ASSISTANT, answer));

        MessageContextResponse responseAfter = langchainClient.appendMessages(
                chat.getId(),
                newMessages
        );
        List<ChatMessage> messageContextAfter = responseAfter.getLangchainChatContext();
        log.info("[Performance] Langchain에 대화 맥락 업데이트 완료: chatId={}, messageCount={}, 소요시간={}ms", chat.getId(), messageContextAfter.size(), System.currentTimeMillis() - contextUpdateStartTime);

        // LLM 호출(Spring): 추천 질문 생성
        long recommendStartTime = System.currentTimeMillis();
        List<String> recommendedQuestions = qnaQuestionRecommendService.recommendQuestions(input.getQuestion());
        log.info("[Performance] 추천 질문 생성 완료: 추천 질문 수={}, 소요시간={}ms", recommendedQuestions.size(), System.currentTimeMillis() - recommendStartTime);

        // 추천 질문까지 포함된 AI 최종 답변 반환
        return new QnaChatMessageOutput(
                botMsg.getId(),
                ASSISTANT,
                answer,
                messageContextAfter,
                referenceChunks,
                recommendedQuestions,
                botMsg.getCreatedAt(),
                false
        );
    }

    @Override
    public GetQnaChatMessagesOutput getMessages(GetQnaChatMessagesInput input) {
        QnaChat chat = qnaChatRepository.findByLectureIdAndUserId(input.getLectureId(), input.getUserId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다"));

        List<QnaChatMessage> messages = qnaChatMessageRepository.findByQnaChatIdWithCursor(
                chat.getId(), input.getCursor(), input.getLimit());

        boolean hasMore = messages.size() > input.getLimit();
        if (hasMore) {
            messages = messages.subList(0, input.getLimit()); // 초과분 제거
        }

        UUID nextCursor = null;
        if (!messages.isEmpty() && hasMore) {
            // 더 가져올 메시지가 있을 때만 가져온 메세지 중 가장 오래된 메세지로 nextCursor 설정
            nextCursor = messages.get(messages.size() - 1).getId();
        }

        List<GetQnaChatMessagesOutput.MessageItem> messageItems = messages.stream()
                .map(m -> {
                    boolean isLiked = m.getRole() == MessageRole.ASSISTANT ? m.getIsLiked() : false; // USER 메세지는 좋아요 FALSE 고정
                    return new GetQnaChatMessagesOutput.MessageItem(
                            m.getId(),
                            m.getRole().getValue(),
                            m.getContent(),
                            m.getRole() == MessageRole.ASSISTANT ? m.getReferences() : null,
                            m.getCreatedAt(),
                            isLiked
                    );
                })
                .toList();

        return new GetQnaChatMessagesOutput(chat.getId(), messageItems, hasMore, nextCursor);
    }
    
    @Override
    public GetLikedMessagesOutput getLikedMessages(GetLikedMessagesInput input) {
        QnaChat chat = qnaChatRepository.findByLectureIdAndUserId(input.getLectureId(), input.getUserId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다"));

        List<QnaChatMessage> likedMessages = qnaChatMessageRepository.findByQnaChatIdAndIsLikedTrue(chat.getId());

        List<GetLikedMessagesOutput.LikedMessageItem> likedMessageItems = likedMessages.stream()
                .map(message -> new GetLikedMessagesOutput.LikedMessageItem(
                        message.getId(),
                        message.getRole().getValue(),
                        message.getContent(),
                        message.getReferences(),
                        message.getCreatedAt(),
                        true
                ))
                .toList();

        return new GetLikedMessagesOutput(chat.getId(), likedMessageItems); //좋아요 메세지 조회에는 페이지네이션 미적용
    }

    @Override
    @Transactional
    public ToggleLikeMessageOutput toggleLikeMessage(ToggleLikeMessageInput input) {
        QnaChatMessage message = qnaChatMessageRepository.findById(input.getMessageId())
                .orElseThrow(() -> new NotFoundException("메시지를 찾을 수 없습니다"));

        if (message.getRole() == MessageRole.USER) {
            throw new BadRequestException("사용자 메시지는 좋아요할 수 없습니다");
        }

        boolean currentLikedStatus = message.getIsLiked();
        boolean newLikedStatus = !currentLikedStatus;

        message.setIsLiked(newLikedStatus);
        qnaChatMessageRepository.save(message);

        String action = newLikedStatus ? "ADDED" : "REMOVED";
        return new ToggleLikeMessageOutput(newLikedStatus, action);
    }
}