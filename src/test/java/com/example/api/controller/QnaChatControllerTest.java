package com.example.api.controller;

import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.qna.QnaChatMessageRequest;
import com.example.api.exception.BadRequestException;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.QnaChatService;
import com.example.api.service.dto.qna.*;
import com.example.api.util.WithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QnaChatController.class)
@Import({TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class QnaChatControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QnaChatService qnaChatService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JwtProvider jwtProvider;

    private UUID FIXED_USER_ID;
    private UUID LECTURE_ID;
    private UUID CHAT_ID;
    private UUID MESSAGE_ID;

    @BeforeEach
    void setUp() {
        FIXED_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        LECTURE_ID = UUID.randomUUID();
        CHAT_ID = UUID.randomUUID();
        MESSAGE_ID = UUID.randomUUID();
    }

    @Test
    @DisplayName("채팅방 생성 성공")
    @WithMockUser
    void createChatSuccess() throws Exception {
        // Given
        CreateQnaChatOutput output = new CreateQnaChatOutput(CHAT_ID, LocalDateTime.now());
        when(qnaChatService.createQnaChat(any(CreateQnaChatInput.class)))
                .thenReturn(output);

        // When & Then
        mockMvc.perform(post("/v1/lectures/{lectureId}/qna-chat", LECTURE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("채팅방 ID 조회 성공")
    @WithMockUser
    void getQnaChatIdSuccess() throws Exception {
        // Given
        GetQnaChatIdOutput output = new GetQnaChatIdOutput(CHAT_ID);
        when(qnaChatService.getQnaChatId(any(GetQnaChatIdInput.class)))
                .thenReturn(output);

        // When & Then
        mockMvc.perform(get("/v1/lectures/{lectureId}/qna-chat", LECTURE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(CHAT_ID.toString()));
    }

    @Test
    @DisplayName("채팅방 메시지 조회 성공 - 커서 기반 페이지네이션")
    @WithMockUser
    void getMessagesSuccess() throws Exception {
        // Given (Repository에서 DESC로 가져온 순서)
        List<GetQnaChatMessagesOutput.MessageItem> messages = List.of(
                new GetQnaChatMessagesOutput.MessageItem(
                        MESSAGE_ID, "assistant", "재귀 함수는 자기 자신을 호출하는 함수입니다.",
                        List.of(new ReferenceResponse.ReferenceChunkResponse("출처1", 42)),
                        LocalDateTime.now(), true
                ),
                new GetQnaChatMessagesOutput.MessageItem(
                        UUID.randomUUID(), "user", "재귀 함수란 무엇인가요?",null, LocalDateTime.now(), false
                )
        );

        UUID nextCursor = MESSAGE_ID;
        boolean hasMore = false;

        GetQnaChatMessagesOutput output = new GetQnaChatMessagesOutput(
                CHAT_ID, messages, hasMore, nextCursor
        );

        when(qnaChatService.getMessages(any(GetQnaChatMessagesInput.class)))
                .thenReturn(output);

        // When & Then
        mockMvc.perform(get("/v1/lectures/{lectureId}/qna-chat/messages", LECTURE_ID)
                        .param("cursor", "550e8400-e29b-41d4-a716-446655440000")
                        .param("limit", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(CHAT_ID.toString()))

                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("재귀 함수란 무엇인가요?"))
                .andExpect(jsonPath("$.messages[0].isLiked").value(false))
                .andExpect(jsonPath("$.messages[0].references").doesNotExist())

                .andExpect(jsonPath("$.messages[1].role").value("assistant"))
                .andExpect(jsonPath("$.messages[1].content").value("재귀 함수는 자기 자신을 호출하는 함수입니다."))
                .andExpect(jsonPath("$.messages[1].isLiked").value(true))
                .andExpect(jsonPath("$.messages[1].references").isArray())
                .andExpect(jsonPath("$.messages[1].references[0].text").value("출처1"))
                .andExpect(jsonPath("$.messages[1].references[0].page").value(42))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.nextCursor").value(nextCursor.toString()));
    }

    @Test
    @DisplayName("좋아요한 메시지 조회 성공")
    @WithMockUser
    void getLikedMessagesSuccess() throws Exception {
        // Given
        List<GetLikedMessagesOutput.LikedMessageItem> likedMessages = List.of(
                new GetLikedMessagesOutput.LikedMessageItem(
                        MESSAGE_ID, "assistant", "재귀 함수는 자기 자신을 호출하는 함수입니다.",
                        List.of(new ReferenceResponse.ReferenceChunkResponse("출처1", 42)),
                        LocalDateTime.now(), true
                )
        );

        GetLikedMessagesOutput output = new GetLikedMessagesOutput(CHAT_ID, likedMessages);
        when(qnaChatService.getLikedMessages(any(GetLikedMessagesInput.class)))
                .thenReturn(output);

        // When & Then
        mockMvc.perform(get("/v1/lectures/{lectureId}/qna-chat/messages/liked", LECTURE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(CHAT_ID.toString()))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.messages[0].role").value("assistant"))
                .andExpect(jsonPath("$.messages[0].isLiked").value(true))
                .andExpect(jsonPath("$.messages[0].references").isArray())
                .andExpect(jsonPath("$.messages[0].references[0].text").value("출처1"));
    }

    @Test
    @DisplayName("질문 전송 성공")
    @WithMockUser
    void sendMessageSuccess() throws Exception {
        // Given
        QnaChatMessageRequest request = new QnaChatMessageRequest("재귀 함수란 무엇인가요?");

        List<ReferenceResponse.ReferenceChunkResponse> references = List.of(
                new ReferenceResponse.ReferenceChunkResponse("재귀 함수는 자기 자신을 호출하는 함수입니다.", 42)
        );

        QnaChatMessageOutput output = new QnaChatMessageOutput(
                MESSAGE_ID,
                "assistant",
                "재귀 함수는 자기 자신을 호출하는 함수입니다.",
                new ArrayList<>(),
                references,
                List.of("재귀 함수와 반복문의 차이점은?"),
                LocalDateTime.now(),
                false
        );

        when(qnaChatService.ask(any(QnaChatMessageInput.class))).thenReturn(output);

        // When & Then
        mockMvc.perform(post("/v1/lectures/{lectureId}/qna-chat/messages", LECTURE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(MESSAGE_ID.toString()))
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("재귀 함수는 자기 자신을 호출하는 함수입니다."))
                .andExpect(jsonPath("$.isLiked").value(false))
                .andExpect(jsonPath("$.references[0].text").value("재귀 함수는 자기 자신을 호출하는 함수입니다."))
                .andExpect(jsonPath("$.references[0].page").value(42))
                .andExpect(jsonPath("$.recommendedQuestions[0]").value("재귀 함수와 반복문의 차이점은?"));
    }

    @Test
    @DisplayName("좋아요 토글 성공 - 좋아요 추가")
    @WithMockUser
    void toggleLikeMessageSuccess_AddLike() throws Exception {
        // Given
        ToggleLikeMessageOutput output = new ToggleLikeMessageOutput(true, "ADDED");
        when(qnaChatService.toggleLikeMessage(any(ToggleLikeMessageInput.class)))
                .thenReturn(output);

        // When & Then
        mockMvc.perform(post("/v1/lectures/{lectureId}/qna-chat/messages/{messageId}/toggle-like",
                        LECTURE_ID, MESSAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(true))
                .andExpect(jsonPath("$.action").value("ADDED"));
    }

    @Test
    @DisplayName("좋아요 토글 성공 - 좋아요 제거")
    @WithMockUser
    void toggleLikeMessageSuccess_RemoveLike() throws Exception {
        // Given
        ToggleLikeMessageOutput output = new ToggleLikeMessageOutput(false, "REMOVED");
        when(qnaChatService.toggleLikeMessage(any(ToggleLikeMessageInput.class)))
                .thenReturn(output);

        // When & Then
        mockMvc.perform(post("/v1/lectures/{lectureId}/qna-chat/messages/{messageId}/toggle-like",
                        LECTURE_ID, MESSAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(false))
                .andExpect(jsonPath("$.action").value("REMOVED"));
    }

    @Test
    @DisplayName("좋아요 토글 실패 - 사용자 메시지")
    @WithMockUser
    void toggleLikeMessageFail_UserMessage() throws Exception {
        // Given
        when(qnaChatService.toggleLikeMessage(any(ToggleLikeMessageInput.class)))
                .thenThrow(new BadRequestException("사용자 메시지는 좋아요할 수 없습니다"));

        // When & Then
        mockMvc.perform(post("/v1/lectures/{lectureId}/qna-chat/messages/{messageId}/toggle-like",
                        LECTURE_ID, MESSAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }
}