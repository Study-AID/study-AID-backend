package com.example.api.controller;

import com.example.api.controller.dto.qna.*;
import com.example.api.security.jwt.CustomUserDetails;
import com.example.api.service.QnaChatService;
import com.example.api.service.dto.qna.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/qna")
@RequiredArgsConstructor
@Tag(name = "QnA Chat", description = "강의 자료 기반 QnA Chat")
public class QnaChatController {

    private final QnaChatService qnaChatService;

    @PostMapping("/chats")
    public ResponseEntity<CreateQnaChatResponse> createChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateQnaChatRequest request
    ) {
        CreateQnaChatInput input = new CreateQnaChatInput(userDetails.getId(), request.getLectureId());
        CreateQnaChatOutput output = qnaChatService.createQnaChat(input);
        return ResponseEntity.ok(new CreateQnaChatResponse(output.getChatId()));
    }

    public ReadQnaChatResponse readQnaChat(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReadQnaChatInput input = new ReadQnaChatInput(chatId, userDetails.getId());
        ReadQnaChatOutput output = qnaChatService.readQnaChat(input);

        List<ReadQnaChatResponse.MessageItem> dto = output.getMessageItems().stream()
                .map(m -> new ReadQnaChatResponse.MessageItem(m.getQuestion(), m.getAnswer()))
                .toList();

        return new ReadQnaChatResponse(dto);
    }

    @PostMapping("/chats/{chatId}/ask")
    public QnaChatMessageResponse askQuestion(
            @PathVariable UUID chatId,
            @RequestBody QnaChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getId();
        QnaChatMessageInput input = new QnaChatMessageInput(chatId, userId, request.getQuestion());
        QnaChatMessageOutput output = qnaChatService.ask(input);

        return new QnaChatMessageResponse(
            output.getQuestion(),
            output.getAnswer(),
            output.getMessageHistory(),
            output.getRecommendedQuestions()
        );
    }
}
