package com.example.api.controller;

import com.example.api.controller.dto.qna.CreateQnaChatRequest;
import com.example.api.controller.dto.qna.QnaChatMessageRequest;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.QnaChatService;
import com.example.api.service.dto.qna.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; //변경
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration; //변경
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = QnaChatController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@ActiveProfiles("test")
class QnaChatControllerTest {
    // TODO(jin): use authorized user instead of fixed user ID
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private QnaChatService qnaChatService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtProvider jwtProvider;

    private static final UUID FIXED_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID LECTURE_ID = UUID.randomUUID();
    private static final UUID CHAT_ID = UUID.randomUUID();

    @Test
    @DisplayName("채팅방 생성 성공")
    void createChatSuccess() throws Exception {
        // Given
        CreateQnaChatRequest request = new CreateQnaChatRequest();
        request.setLectureId(LECTURE_ID);

        when(qnaChatService.createQnaChat(any(CreateQnaChatInput.class)))
                .thenReturn(new CreateQnaChatOutput(CHAT_ID));

        // When & Then
        mockMvc.perform(post("/v1/qna/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").exists());
    }

    @Test
    @DisplayName("채팅방 메시지 조회 성공")
    void readChatSuccess() throws Exception {
        // Given
        List<ReadQnaChatOutput.MessageItem> messages = List.of(
                new ReadQnaChatOutput.MessageItem("user", "재귀 함수란 무엇인가요?"),
                new ReadQnaChatOutput.MessageItem("assistant", "재귀 함수는 자기 자신을 호출하는 함수입니다.")
        );

        // chatId 추가
        when(qnaChatService.readQnaChat(any(ReadQnaChatInput.class)))
                .thenReturn(new ReadQnaChatOutput(CHAT_ID, messages));

        // When & Then
        mockMvc.perform(get("/v1/qna/chats/" + CHAT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").exists())
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("재귀 함수란 무엇인가요?"))
                .andExpect(jsonPath("$.messages[1].role").value("assistant"))
                .andExpect(jsonPath("$.messages[1].content").value("재귀 함수는 자기 자신을 호출하는 함수입니다."));
    }

    @Test
    @DisplayName("질문 전송 성공")
    void askQuestionSuccess() throws Exception {
        // Given
        QnaChatMessageRequest request = new QnaChatMessageRequest("재귀 함수란 무엇인가요?");

        List<ReferenceResponse.ReferenceChunkResponse> references =
                List.of(new ReferenceResponse.ReferenceChunkResponse("text", 42));

        QnaChatMessageOutput output = new QnaChatMessageOutput(
                "assistant",
                "재귀 함수는 자기 자신을 호출하는 함수입니다.",
                new ArrayList<>(),
                references,
                List.of("재귀 함수와 반복문의 차이점은?")
        );

        when(qnaChatService.ask(any())).thenReturn(output);

        // When & Then
        mockMvc.perform(post("/v1/qna/chats/{chatId}/ask", CHAT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("재귀 함수는 자기 자신을 호출하는 함수입니다."))
                .andExpect(jsonPath("$.messageContext").doesNotExist())
                .andExpect(jsonPath("$.references[0].text").value("text"))
                .andExpect(jsonPath("$.references[0].page").value(42))
                .andExpect(jsonPath("$.recommendedQuestions[0]").value("재귀 함수와 반복문의 차이점은?"));
    }
}