package com.example.api.service;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.entity.*;
import com.example.api.entity.enums.MessageRole;
import com.example.api.exception.NotFoundException;
import com.example.api.exception.UnauthorizedException;
import com.example.api.external.LangchainClient;
import com.example.api.external.dto.langchain.MessageContextResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.QnaChatMessageRepository;
import com.example.api.repository.QnaChatRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.qna.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        ParsedPage parsedPage = new ParsedPage(1, "재귀 함수는 자기 자신을 호출하는 함수입니다.");
        ParsedText parsedText = new ParsedText(1, List.of(parsedPage));
        testLecture.setParsedText(parsedText);

        testQnaChat = new QnaChat();
        testQnaChat.setId(TEST_CHAT_ID);
        testQnaChat.setUser(testUser);
        testQnaChat.setLecture(testLecture);
    }

    @Test
    @DisplayName("채팅방 생성 성공 테스트")
    public void createQnaChatSuccessTest() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(lectureRepository.findById(TEST_LECTURE_ID)).thenReturn(Optional.of(testLecture));
        when(qnaChatRepository.save(any(QnaChat.class))).thenAnswer(invocation -> {
            QnaChat savedChat = invocation.getArgument(0);
            savedChat.setId(TEST_CHAT_ID);
            return savedChat;
        });

        CreateQnaChatInput input = new CreateQnaChatInput(TEST_USER_ID, TEST_LECTURE_ID);
        CreateQnaChatOutput output = qnaChatService.createQnaChat(input);

        assertNotNull(output);
        assertEquals(TEST_CHAT_ID, output.getChatId());
    }

    @Test
    @DisplayName("채팅방 생성 실패 테스트 - 사용자 없음")
    public void createQnaChatFailUserNotFoundTest() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());
        CreateQnaChatInput input = new CreateQnaChatInput(TEST_USER_ID, TEST_LECTURE_ID);
        assertThrows(NotFoundException.class, () -> qnaChatService.createQnaChat(input));
    }

    @Test
    @DisplayName("채팅 내용 조회 성공 테스트")
    public void getMessagesSuccessTest() {
        when(qnaChatRepository.findById(TEST_CHAT_ID)).thenReturn(Optional.of(testQnaChat));

        List<QnaChatMessage> messages = new ArrayList<>();
        QnaChatMessage message1 = new QnaChatMessage();

        message1.setRole(MessageRole.USER);
        message1.setContent("재귀 함수란 무엇인가요?");
        messages.add(message1);

        QnaChatMessage message2 = new QnaChatMessage();
        message2.setRole(MessageRole.ASSISTANT);
        message2.setContent("재귀 함수는 자기 자신을 호출하는 함수입니다.");
        messages.add(message2);

        when(qnaChatMessageRepository.findByQnaChatId(TEST_CHAT_ID)).thenReturn(messages);

        ReadQnaChatInput input = new ReadQnaChatInput(TEST_CHAT_ID, TEST_USER_ID);
        ReadQnaChatOutput output = qnaChatService.getMessages(input);

        assertNotNull(output);
        assertEquals(2, output.getMessages().size());
        assertEquals("user", output.getMessages().get(0).getRole());
        assertEquals("재귀 함수란 무엇인가요?", output.getMessages().get(0).getContent());
        assertEquals("assistant", output.getMessages().get(1).getRole());
        assertEquals("재귀 함수는 자기 자신을 호출하는 함수입니다.", output.getMessages().get(1).getContent());
    }

    @Test
    @DisplayName("채팅 내용 조회 실패 테스트 - 권한 없음")
    public void getMessagesFailUnauthorizedTest() {
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User(otherUserId);
        QnaChat otherChat = new QnaChat();
        otherChat.setId(TEST_CHAT_ID);
        otherChat.setUser(otherUser);

        when(qnaChatRepository.findById(TEST_CHAT_ID)).thenReturn(Optional.of(otherChat));
        ReadQnaChatInput input = new ReadQnaChatInput(TEST_CHAT_ID, TEST_USER_ID);

        assertThrows(UnauthorizedException.class, () -> qnaChatService.getMessages(input));
    }

    @Test
    @DisplayName("질문 요청 성공 테스트")
    public void askSuccessTest() {
        String question = "재귀 함수란 무엇인가요?";
        String answer = "재귀 함수는 자기 자신을 호출하는 함수입니다.";

        when(qnaChatRepository.findById(TEST_CHAT_ID)).thenReturn(Optional.of(testQnaChat));
        when(lectureRepository.findById(TEST_LECTURE_ID)).thenReturn(Optional.of(testLecture));
        when(qnaChatMessageRepository.save(any(QnaChatMessage.class))).thenAnswer(i -> i.getArgument(0));

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

        QnaChatMessageInput input = new QnaChatMessageInput(TEST_CHAT_ID, TEST_USER_ID, question);
        QnaChatMessageOutput output = qnaChatService.ask(input);

        assertNotNull(output);
        assertEquals("assistant", output.getRole());
        assertEquals(answer, output.getContent());
        assertEquals(1, output.getReferences().size());
        assertEquals("재귀 함수는 자기 자신을 호출하는 함수입니다.", output.getReferences().get(0).getText());
        assertEquals(42, output.getReferences().get(0).getPage());
        assertEquals(1, output.getRecommendedQuestions().size());
        assertEquals("재귀 함수의 장단점은 무엇인가요?", output.getRecommendedQuestions().get(0));
    }

    @Test
    @DisplayName("질문 요청 실패 테스트 - 채팅방 없음")
    public void askFailChatNotFoundTest() {
        when(qnaChatRepository.findById(TEST_CHAT_ID)).thenReturn(Optional.empty());
        QnaChatMessageInput input = new QnaChatMessageInput(TEST_CHAT_ID, TEST_USER_ID, "질문");
        assertThrows(NotFoundException.class, () -> qnaChatService.ask(input));
    }

    @Test
    @DisplayName("질문 요청 실패 테스트 - 권한 없음")
    public void askFailUnauthorizedTest() {
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User(otherUserId);
        QnaChat otherChat = new QnaChat();
        otherChat.setId(TEST_CHAT_ID);
        otherChat.setUser(otherUser);

        when(qnaChatRepository.findById(TEST_CHAT_ID)).thenReturn(Optional.of(otherChat));
        QnaChatMessageInput input = new QnaChatMessageInput(TEST_CHAT_ID, TEST_USER_ID, "질문");
        assertThrows(UnauthorizedException.class, () -> qnaChatService.ask(input));
    }
}