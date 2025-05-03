package com.example.api.controller;

import com.example.api.controller.dto.qna.QnaChatRequest;
import com.example.api.controller.dto.qna.QnaChatResponse;
import com.example.api.service.QnaChatService;
import com.example.api.service.dto.qna.QnaChatInput;
import com.example.api.service.dto.qna.QnaChatOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/qna-chats")
@RequiredArgsConstructor
public class QnaChatController {

    private final QnaChatService qnaChatService;

    @PostMapping("/{chatId}/ask")
    public QnaChatResponse askQuestion(
            @PathVariable UUID chatId,
            @RequestBody QnaChatRequest request,
            @RequestHeader("X-USER-ID") UUID userId
    ) {
        QnaChatInput input = new QnaChatInput(chatId, userId, request.getQuestion());
        QnaChatOutput output = qnaChatService.ask(input);

        return new QnaChatResponse(
                output.getQuestion(),
                output.getAnswer(),
                output.getSource(),
                output.getRecommendedQuestions()
        );
    }
}
