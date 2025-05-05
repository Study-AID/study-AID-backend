package com.example.api.controller;

import com.example.api.controller.dto.qna.*;
import com.example.api.service.QnaChatService;
import com.example.api.service.dto.qna.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/qna")
@RequiredArgsConstructor
@Tag(name = "QnA Chat", description = "강의 자료 기반 QnA Chat API")
public class QnaChatController {
    // TODO(jin): use authorized user instead of fixed user ID

    private final QnaChatService qnaChatService;

    private static final UUID FIXED_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Operation(
            summary = "QnA 채팅방 생성",
            description = "강의 자료를 기반으로 새로운 QnA 채팅방을 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 생성 성공",
                            content = @Content(schema = @Schema(implementation = CreateQnaChatResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{\"chatId\": \"550e8400-e29b-41d4-a716-446655440000\"}"
                                            )
                                    })
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"잘못된 요청입니다\", \"code\": \"BAD_REQUEST\", \"timestamp\": 1699541415123}"
                                    )
                            })
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "강의 자료나 사용자를 찾을 수 없음",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"강의 자료를 찾을 수 없습니다\", \"code\": \"NOT_FOUND\", \"timestamp\": 1699541415123}"
                                    )
                            })
                    )
            }
    )
    @PostMapping(value = "/chats")
    public ResponseEntity<CreateQnaChatResponse> createChat(@RequestBody CreateQnaChatRequest request) {
        CreateQnaChatInput input = new CreateQnaChatInput(FIXED_USER_ID, request.getLectureId());
        CreateQnaChatOutput output = qnaChatService.createQnaChat(input);
        return ResponseEntity.ok(new CreateQnaChatResponse(output.getChatId()));
    }

    @Operation(
            summary = "특정 QnA 채팅방 조회 (모든 메시지 조회)",
            description = "특정 QnA 채팅방의 모든 메시지를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅 메시지 조회 성공",
                            content = @Content(schema = @Schema(implementation = ReadQnaChatResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{\"messages\": " +
                                                                "[{\"question\": \"재귀 함수란 무엇인가요?\", " +
                                                                "\"answer\": \"재귀 함수는 자기 자신을 호출하는 함수를 의미합니다. 함수 내부에서 자신을 다시 호출하는 방식으로 동작하며, 복잡한 문제를 간단하게 해결할 수 있는 프로그래밍 기법입니다.\"}]}"
                                            )
                                    })
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "접근 권한 없음",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"해당 채팅방에 대한 접근 권한이 없습니다\", \"code\": \"UNAUTHORIZED\", \"timestamp\": 1699541415123}"
                                    )
                            })
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "채팅방을 찾을 수 없음",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"채팅방을 찾을 수 없습니다\", \"code\": \"NOT_FOUND\", \"timestamp\": 1699541415123}"
                                    )
                            })
                    )
            }
    )
    @GetMapping(value = "/chats/{chatId}")
    public ResponseEntity<ReadQnaChatResponse> readQnaChat(@PathVariable UUID chatId) {
        ReadQnaChatInput input = new ReadQnaChatInput(chatId, FIXED_USER_ID);
        ReadQnaChatOutput output = qnaChatService.readQnaChat(input);

        List<ReadQnaChatResponse.MessageItem> dto = output.getMessages().stream()
                .map(m -> new ReadQnaChatResponse.MessageItem(m.getQuestion(), m.getAnswer()))
                .toList();

        return ResponseEntity.ok(new ReadQnaChatResponse(dto));
    }

    @Operation(
            summary = "질문하기",
            description = "QnA 채팅방에 질문을 하고 강의 자료를 기반으로 답변을 받습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "질문 처리 성공",
                            content = @Content(schema = @Schema(implementation = QnaChatMessageResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{\"question\": \"재귀 함수란 무엇인가요?\", " +
                                                            "\"answer\": \"📝자료에 따르면, 재귀 함수는 자기 자신을 호출하는 함수를 의미합니다. 함수 내부에서 자신을 다시 호출하는 방식으로 동작하며, 복잡한 문제를 간단하게 해결할 수 있는 프로그래밍 기법입니다. 🤖추가적으로, 제가 알기로는 재귀 함수는 종료 조건이 반드시 필요하며, 그렇지 않으면 무한 루프에 빠질 수 있습니다.\", " +
                                                            "\"messageHistory\": [" +
                                                                "{\"role\":\"user\",\"content\":\"자료구조에서 스택의 개념이 무엇인가요?\"}, " +
                                                                "{\"role\":\"assistant\",\"content\":\"📝자료에 따르면, 스택은 후입선출(LIFO) 방식으로 동작하는 자료구조입니다. 가장 최근에 추가된 항목이 가장 먼저 제거됩니다.\"}, " +
                                                                "{\"role\":\"user\",\"content\":\"재귀 함수란 무엇인가요?\"}], " +
                                                            "\"references\": [" +
                                                                "{\"text\": \"재귀 함수(recursion)는 컴퓨터 과학에서 자기 자신을 호출하는 함수 또는 알고리즘을 말합니다. 이는 복잡한 문제를 더 작고 관리하기 쉬운 부분 문제로 나누어 해결하는 방법입니다.\", \"page\": 42}, " +
                                                                "{\"text\": \"재귀 함수는 기저 사례(base case)가 필요합니다. 기저 사례는 재귀 호출을 멈추는 조건입니다.\", \"page\": 43}], " +
                                                            "\"recommendedQuestions\": [\"재귀 함수의 장단점은 무엇인가요?\", \"재귀 함수와 반복문의 차이점은 무엇인가요?\", \"재귀 함수에서 기저 사례(base case)란 무엇인가요?\"]}"
                                            )
                                    })
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"잘못된 요청입니다\", \"code\": \"BAD_REQUEST\", \"timestamp\": 1699541415123}"
                                    )
                            })
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "접근 권한 없음",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"해당 채팅방에 대한 접근 권한이 없습니다\", \"code\": \"UNAUTHORIZED\", \"timestamp\": 1699541415123}"
                                    )
                            })
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "채팅방을 찾을 수 없음",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"채팅방을 찾을 수 없습니다\", \"code\": \"NOT_FOUND\", \"timestamp\": 1699541415123}"
                                    )
                            })
                    )
            }
    )
    @PostMapping(value = "/chats/{chatId}/ask")
    public ResponseEntity<QnaChatMessageResponse> askQuestion(
            @PathVariable UUID chatId,
            @RequestBody QnaChatMessageRequest request) {

        QnaChatMessageInput input = new QnaChatMessageInput(chatId, FIXED_USER_ID, request.getQuestion());
        QnaChatMessageOutput output = qnaChatService.ask(input);

        QnaChatMessageResponse response = new QnaChatMessageResponse(
                output.getQuestion(),
                output.getAnswer(),
                output.getReferences(),
                output.getMessageHistory(),
                output.getRecommendedQuestions()
        );

        return ResponseEntity.ok(response);
    }
}