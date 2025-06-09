package com.example.api.service;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.entity.*;
import com.example.api.entity.enums.MessageRole;
import com.example.api.exception.BadRequestException;
import com.example.api.exception.NotFoundException;
import com.example.api.external.LangchainClient;
import com.example.api.external.dto.langchain.EmbeddingCheckResponse;
import com.example.api.external.dto.langchain.MessageContextResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.repository.*;
import com.example.api.service.dto.qna.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QnaChatServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private QnaChatRepository qnaChatRepository;
    @Mock
    private QnaChatMessageRepository qnaChatMessageRepository;
    @Mock
    private LangchainClient langchainClient;
    @Mock
    private QnaQuestionRecommendService qnaQuestionRecommendService;
    @Mock
    private LLMAdapter llmAdapter;

    @InjectMocks
    private QnaChatServiceImpl qnaChatService;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_LECTURE_ID = UUID.randomUUID();
    private static final UUID TEST_CHAT_ID = UUID.randomUUID();
    private static final UUID TEST_MESSAGE_ID = UUID.randomUUID();

    private User testUser;
    private Lecture testLecture;
    private QnaChat testQnaChat;

    @BeforeEach
    public void setup() {
        testUser = new User(TEST_USER_ID);
        testUser.setName("테스트사용자");
        testUser.setEmail("test@example.com");

        testLecture = new Lecture();
        testLecture.setId(TEST_LECTURE_ID);
        testLecture.setTitle("테스트 강의");
        testLecture.setIsVectorized(true);
        ParsedPage parsedPage = new ParsedPage(1, "재귀 함수는 자기 자신을 호출하는 함수입니다.");
        ParsedText parsedText = new ParsedText(1, List.of(parsedPage));
        testLecture.setParsedText(parsedText);

        testQnaChat = new QnaChat();
        testQnaChat.setId(TEST_CHAT_ID);
        testQnaChat.setUser(testUser);
        testQnaChat.setLecture(testLecture);
        testQnaChat.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("채팅방 생성 성공 테스트 - 강의의 기존 채팅방 없을 때")
    public void createQnaChatSuccessTest() {
        // Given
        when(lectureRepository.findById(TEST_LECTURE_ID)).thenReturn(Optional.of(testLecture));
        when(qnaChatRepository.findByLectureIdAndUserId(TEST_LECTURE_ID, TEST_USER_ID))
                .thenReturn(Optional.empty());
        when(qnaChatRepository.save(any(QnaChat.class))).thenAnswer(invocation -> {
            QnaChat savedChat = invocation.getArgument(0);
            savedChat.setId(TEST_CHAT_ID);
            savedChat.setCreatedAt(LocalDateTime.now());
            return savedChat;
        });

        EmbeddingCheckResponse mockResponse = new EmbeddingCheckResponse();
        when(langchainClient.checkEmbeddingStatus(TEST_LECTURE_ID))
                .thenReturn(mockResponse);

        // When
        CreateQnaChatInput input = new CreateQnaChatInput(TEST_USER_ID, TEST_LECTURE_ID);
        CreateQnaChatOutput output = qnaChatService.createQnaChat(input);

        // Then
        assertNotNull(output);
        assertEquals(TEST_CHAT_ID, output.getChatId());
        assertNotNull(output.getCreatedAt());
    }

    @Test
    @DisplayName("채팅방 생성 성공 테스트 - 강의의 기존 채팅방 반환")
    public void createQnaChatSuccessTest_ExistingChat() {
        // Given
        when(lectureRepository.findById(TEST_LECTURE_ID)).thenReturn(Optional.of(testLecture));
        when(qnaChatRepository.findByLectureIdAndUserId(TEST_LECTURE_ID, TEST_USER_ID))
                .thenReturn(Optional.of(testQnaChat));

        // When
        CreateQnaChatInput input = new CreateQnaChatInput(TEST_USER_ID, TEST_LECTURE_ID);
        CreateQnaChatOutput output = qnaChatService.createQnaChat(input);

        // Then
        assertNotNull(output);
        assertEquals(TEST_CHAT_ID, output.getChatId());
        assertNotNull(output.getCreatedAt());
    }

    @Test
    @DisplayName("채팅방 ID 조회 성공 테스트")
    public void getQnaChatIdSuccessTest() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(qnaChatRepository.findByLectureIdAndUserId(TEST_LECTURE_ID, TEST_USER_ID))
                .thenReturn(Optional.of(testQnaChat));

        // When
        GetQnaChatIdInput input = new GetQnaChatIdInput(TEST_LECTURE_ID, TEST_USER_ID);
        GetQnaChatIdOutput output = qnaChatService.getQnaChatId(input);

        // Then
        assertNotNull(output);
        assertEquals(TEST_CHAT_ID, output.getChatId());
        assertTrue(output.isVectorized());
    }

    @Test
    @DisplayName("채팅 내용 조회 성공 테스트")
    public void getMessagesSuccessTest() {
        // Given
        when(qnaChatRepository.findByLectureIdAndUserId(TEST_LECTURE_ID, TEST_USER_ID))
                .thenReturn(Optional.of(testQnaChat));

        QnaChatMessage message1 = new QnaChatMessage();
        message1.setId(UUID.randomUUID());
        message1.setRole(MessageRole.USER);
        message1.setContent("재귀 함수란 무엇인가요?");
        message1.setCreatedAt(LocalDateTime.now());
        message1.setIsLiked(false);
        message1.setReferences(null);

        QnaChatMessage message2 = new QnaChatMessage();
        message2.setId(TEST_MESSAGE_ID);
        message2.setRole(MessageRole.ASSISTANT);
        message2.setContent("재귀 함수는 자기 자신을 호출하는 함수입니다.");
        message2.setCreatedAt(LocalDateTime.now());
        message2.setIsLiked(true);
        message2.setReferences(List.of(new ReferenceResponse.ReferenceChunkResponse("출처1", 42)));

        when(qnaChatMessageRepository.findByQnaChatIdWithCursor(eq(TEST_CHAT_ID), eq(TEST_MESSAGE_ID), eq(20)))
                .thenReturn(List.of(message1, message2));

        // When
        GetQnaChatMessagesInput input = new GetQnaChatMessagesInput(TEST_LECTURE_ID, TEST_USER_ID, TEST_MESSAGE_ID, 20);
        GetQnaChatMessagesOutput output = qnaChatService.getMessages(input);

        // Then
        assertNotNull(output);
        assertEquals(TEST_CHAT_ID, output.getChatId());
        assertEquals(2, output.getMessages().size());
        assertEquals("user", output.getMessages().get(0).getRole());
        assertEquals("재귀 함수란 무엇인가요?", output.getMessages().get(0).getContent());
        assertNotNull(output.getMessages().get(0).getMessageId());
        assertNotNull(output.getMessages().get(0).getCreatedAt());
        assertFalse(output.getMessages().get(0).isLiked());
        assertNull(output.getMessages().get(0).getReferences());
        assertEquals("assistant", output.getMessages().get(1).getRole());
        assertEquals("재귀 함수는 자기 자신을 호출하는 함수입니다.", output.getMessages().get(1).getContent());
        assertTrue(output.getMessages().get(1).isLiked());
        assertNotNull(output.getMessages().get(1).getReferences());
        assertEquals(1, output.getMessages().get(1).getReferences().size());
        assertEquals("출처1", output.getMessages().get(1).getReferences().get(0).getText());
    }

    @Test
    @DisplayName("질문 요청 성공 테스트")
    public void askSuccessTest() {
        // Given
        String question = "재귀 함수란 무엇인가요?";
        String answer = "재귀 함수는 자기 자신을 호출하는 함수입니다.";

        when(qnaChatRepository.findByLectureIdAndUserId(TEST_LECTURE_ID, TEST_USER_ID))
                .thenReturn(Optional.of(testQnaChat));
        when(lectureRepository.findById(TEST_LECTURE_ID)).thenReturn(Optional.of(testLecture));
        when(qnaChatMessageRepository.save(any(QnaChatMessage.class))).thenAnswer(invocation -> {
            QnaChatMessage msg = invocation.getArgument(0);
            msg.setId(TEST_MESSAGE_ID);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        EmbeddingCheckResponse mockResponse = new EmbeddingCheckResponse();
        when(langchainClient.checkEmbeddingStatus(TEST_LECTURE_ID))
                .thenReturn(mockResponse);

        // 출처 모의
        ReferenceResponse referenceResponse = new ReferenceResponse();
        List<ReferenceResponse.ReferenceChunkResponse> references = new ArrayList<>();
        references.add(new ReferenceResponse.ReferenceChunkResponse("재귀 함수는 자기 자신을 호출하는 함수입니다.", 42));
        referenceResponse.setReferences(references);
        when(langchainClient.findReferencesInLecture(eq(TEST_LECTURE_ID), eq(question), eq(3), eq(0.3)))
                .thenReturn(referenceResponse);

        // 대화 히스토리 모의
        List<ChatMessage> messageHistory = new ArrayList<>();
        messageHistory.add(new ChatMessage("user", "재귀 함수란?"));
        messageHistory.add(new ChatMessage("assistant", "자기 자신을 호출하는 함수입니다."));
        when(langchainClient.getMessageContext(TEST_CHAT_ID))
                .thenReturn(new MessageContextResponse(messageHistory));

        // LLM 응답
        List<String> referenceTexts = references.stream().map(ReferenceResponse.ReferenceChunkResponse::getText).toList();
        when(llmAdapter.ask(eq(question), eq(referenceTexts), eq(messageHistory)))
                .thenReturn(answer);

        // 대화 저장 응답
        when(langchainClient.appendMessages(any(UUID.class), anyList()))
                .thenReturn(new MessageContextResponse(messageHistory));

        // 추천 질문 모의
        List<String> recommendQuestions = List.of("재귀 함수의 장단점은 무엇인가요?");
        when(qnaQuestionRecommendService.recommendQuestions(eq(question)))
                .thenReturn(recommendQuestions);

        // When
        QnaChatMessageInput input = new QnaChatMessageInput(TEST_LECTURE_ID, TEST_USER_ID, question);
        QnaChatMessageOutput output = qnaChatService.ask(input);

        // Then
        assertNotNull(output);
        assertEquals(TEST_MESSAGE_ID, output.getMessageId());
        assertEquals("assistant", output.getRole());
        assertEquals(answer, output.getContent());
        assertNotNull(output.getCreatedAt());
        assertFalse(output.isLiked());
        assertEquals(1, output.getReferences().size());
        assertEquals("재귀 함수는 자기 자신을 호출하는 함수입니다.", output.getReferences().get(0).getText());
        assertEquals("재귀 함수의 장단점은 무엇인가요?", output.getRecommendedQuestions().get(0));
    }

    @Test
    @DisplayName("좋아요 토글 성공 테스트 - 좋아요 추가")
    public void toggleLikeMessageSuccess_AddLike() {
        // Given
        QnaChatMessage testMessage = new QnaChatMessage();
        testMessage.setId(TEST_MESSAGE_ID);
        testMessage.setRole(MessageRole.ASSISTANT);
        testMessage.setContent("재귀 함수는 자기 자신을 호출하는 함수입니다.");
        testMessage.setQnaChat(testQnaChat);
        testMessage.setUser(testUser);
        testMessage.setIsLiked(false);

        when(qnaChatMessageRepository.findById(TEST_MESSAGE_ID)).thenReturn(Optional.of(testMessage));
        when(qnaChatMessageRepository.save(any(QnaChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ToggleLikeMessageInput input = new ToggleLikeMessageInput(TEST_MESSAGE_ID, TEST_USER_ID);
        ToggleLikeMessageOutput output = qnaChatService.toggleLikeMessage(input);

        // Then
        assertNotNull(output);
        assertTrue(output.isLiked());
        assertEquals("ADDED", output.getAction());
        assertTrue(testMessage.getIsLiked());
    }

    @Test
    @DisplayName("좋아요 토글 성공 테스트 - 좋아요 제거")
    public void toggleLikeMessageSuccess_RemoveLike() {
        // Given
        QnaChatMessage testMessage = new QnaChatMessage();
        testMessage.setId(TEST_MESSAGE_ID);
        testMessage.setRole(MessageRole.ASSISTANT);
        testMessage.setContent("재귀 함수는 자기 자신을 호출하는 함수입니다.");
        testMessage.setQnaChat(testQnaChat);
        testMessage.setUser(testUser);
        testMessage.setIsLiked(true);

        when(qnaChatMessageRepository.findById(TEST_MESSAGE_ID)).thenReturn(Optional.of(testMessage));
        when(qnaChatMessageRepository.save(any(QnaChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ToggleLikeMessageInput input = new ToggleLikeMessageInput(TEST_MESSAGE_ID, TEST_USER_ID);
        ToggleLikeMessageOutput output = qnaChatService.toggleLikeMessage(input);

        // Then
        assertNotNull(output);
        assertFalse(output.isLiked());
        assertEquals("REMOVED", output.getAction());
        assertFalse(testMessage.getIsLiked());
    }

    @Test
    @DisplayName("좋아요 토글 실패 테스트 - 사용자 메시지")
    public void toggleLikeMessageFail_UserMessage() {
        // Given
        QnaChatMessage userMessage = new QnaChatMessage();
        userMessage.setId(TEST_MESSAGE_ID);
        userMessage.setRole(MessageRole.USER);
        userMessage.setContent("재귀 함수란 무엇인가요?");
        userMessage.setQnaChat(testQnaChat);
        userMessage.setUser(testUser);

        when(qnaChatMessageRepository.findById(TEST_MESSAGE_ID)).thenReturn(Optional.of(userMessage));

        // When & Then
        ToggleLikeMessageInput input = new ToggleLikeMessageInput(TEST_MESSAGE_ID, TEST_USER_ID);
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> qnaChatService.toggleLikeMessage(input));
        assertEquals("사용자 메시지는 좋아요할 수 없습니다", exception.getMessage());
    }

    @Test
    @DisplayName("좋아요한 메시지 조회 성공 테스트")
    public void getLikedMessagesSuccess() {
        // Given
        QnaChatMessage likedMessage = new QnaChatMessage();
        likedMessage.setId(TEST_MESSAGE_ID);
        likedMessage.setRole(MessageRole.ASSISTANT);
        likedMessage.setContent("재귀 함수는 자기 자신을 호출하는 함수입니다.");
        likedMessage.setCreatedAt(LocalDateTime.now());
        likedMessage.setIsLiked(true);
        likedMessage.setReferences(List.of(new ReferenceResponse.ReferenceChunkResponse("출처1", 42)));

        when(qnaChatRepository.findByLectureIdAndUserId(TEST_LECTURE_ID, TEST_USER_ID))
                .thenReturn(Optional.of(testQnaChat));
        when(qnaChatMessageRepository.findByQnaChatIdAndIsLikedTrue(TEST_CHAT_ID))
                .thenReturn(List.of(likedMessage));

        // When
        GetLikedMessagesInput input = new GetLikedMessagesInput(TEST_LECTURE_ID, TEST_USER_ID);
        GetLikedMessagesOutput output = qnaChatService.getLikedMessages(input);

        // Then
        assertNotNull(output);
        assertEquals(TEST_CHAT_ID, output.getChatId());
        assertEquals(1, output.getMessages().size());
        assertEquals(TEST_MESSAGE_ID, output.getMessages().get(0).getMessageId());
        assertEquals("assistant", output.getMessages().get(0).getRole());
        assertEquals("재귀 함수는 자기 자신을 호출하는 함수입니다.", output.getMessages().get(0).getContent());
        assertTrue(output.getMessages().get(0).isLiked());
        assertNotNull(output.getMessages().get(0).getReferences());
        assertEquals(1, output.getMessages().get(0).getReferences().size());
        assertEquals("출처1", output.getMessages().get(0).getReferences().get(0).getText());
    }
}