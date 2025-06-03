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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/lectures/{lectureId}/qna-chat")
@RequiredArgsConstructor
@Tag(name = "QnA Chat", description = "강의 자료 기반 QnA Chat API")
public class QnaChatController {
    private final QnaChatService qnaChatService;

    private UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 현재 인증된 사용자 정보 가져오기
        if (authentication == null || authentication.getPrincipal() == null) {

            return UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // 테스트 환경용 fallback
        }
        return UUID.fromString(authentication.getPrincipal().toString());
    }

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
                                                    value = "{\"chatId\": \"550e8400-e29b-41d4-a716-446655440000\", \"createdAt\": \"2024-05-23T10:30:00Z\"}"
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
    @PostMapping
    public ResponseEntity<CreateQnaChatResponse> createChat(@PathVariable UUID lectureId) {
        UUID userId = getUserId();
        CreateQnaChatInput input = new CreateQnaChatInput(userId, lectureId);
        CreateQnaChatOutput output = qnaChatService.createQnaChat(input);
        CreateQnaChatResponse response = new CreateQnaChatResponse(
                output.getChatId(),
                output.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "특정 강의의 QnA 채팅방 UUID 조회",
            description = "특정 강의의 QnA 채팅방 UUID를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 UUID 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetQnaChatIdResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{\"chatId\": \"550e8400-e29b-41d4-a716-446655440000\"}"
                                            )
                                    })
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "채팅방을 찾을 수 없음"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<GetQnaChatIdResponse> getQnaChatId(@PathVariable UUID lectureId) {
        UUID userId = getUserId();
        GetQnaChatIdInput input = new GetQnaChatIdInput(lectureId, userId);
        GetQnaChatIdOutput output = qnaChatService.getQnaChatId(input);

        GetQnaChatIdResponse response = new GetQnaChatIdResponse(output.getChatId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "특정 QnA 채팅방 모든 메시지 조회",
            description = "특정 QnA 채팅방의 모든 메시지를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅 메시지 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetQnaChatMessagesResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{" +
                                                            "\"chatId\": \"550e8400-e29b-41d4-a716-446655440000\", " +
                                                            "\"messages\": [" +
                                                            "{" +
                                                            "\"messageId\": \"msg-550e8400-e29b-41d4-a716-446655440001\", " +
                                                            "\"role\": \"user\", " +
                                                            "\"content\": \"재귀 함수란 무엇인가요?\", " +
                                                            "\"createdAt\": \"2025-05-28T04:15:00Z\", " +
                                                            "\"isLiked\": false" +
                                                            "}, " +
                                                            "{" +
                                                            "\"messageId\": \"msg-550e8400-e29b-41d4-a716-446655440002\", " +
                                                            "\"role\": \"assistant\", " +
                                                            "\"content\": \"재귀 함수는 자기 자신을 호출하는 함수를 의미합니다. 함수 내부에서 자신을 다시 호출하는 방식으로 동작하며, 복잡한 문제를 간단하게 해결할 수 있는 프로그래밍 기법입니다.\", " +
                                                            "\"createdAt\": \"2025-05-28T04:15:30Z\", " +
                                                            "\"isLiked\": true" +
                                                            "}" +
                                                            "]," +
                                                            "\"hasMore\": true, " +
                                                            "\"nextCursor\": \"msg-550e8400-e29b-41d4-a716-446655440002\"" +
                                                            "}"
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
    @GetMapping(value = "/messages")
    public ResponseEntity<GetQnaChatMessagesResponse> getMessages(
            @PathVariable UUID lectureId,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        UUID userId = getUserId();
        GetQnaChatMessagesInput input = new GetQnaChatMessagesInput(lectureId, userId, cursor, limit);
        GetQnaChatMessagesOutput output = qnaChatService.getMessages(input);

        List<GetQnaChatMessagesResponse.MessageItem> messages = output.getMessages().stream()
                .map(m -> new GetQnaChatMessagesResponse.MessageItem(
                        m.getMessageId(),
                        m.getRole(),
                        m.getContent(),
                        m.getCreatedAt(),
                        m.isLiked()
                ))
                .toList();

        GetQnaChatMessagesResponse response = new GetQnaChatMessagesResponse(
                output.getChatId(),
                messages,
                output.getHasMore(),
                output.getNextCursor()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "QnA 채팅방 좋아요한 메시지 조회",
            description = "특정 강의의 QnA 채팅방에서 좋아요한 메시지들만 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요한 메시지 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetQnaChatMessagesResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{" +
                                                            "\"chatId\": \"550e8400-e29b-41d4-a716-446655440000\", " +
                                                            "\"messages\": [" +
                                                            "{" +
                                                            "\"messageId\": \"msg-550e8400-e29b-41d4-a716-446655440002\", " +
                                                            "\"role\": \"assistant\", " +
                                                            "\"content\": \"재귀 함수는 자기 자신을 호출하는 함수를 의미합니다.\", " +
                                                            "\"createdAt\": \"2025-05-28T04:15:30Z\", " +
                                                            "\"isLiked\": true" +
                                                            "}" +
                                                            "]" +
                                                            "}"
                                            )
                                    })
                    )
            }
    )
    @GetMapping("/messages/liked")
    public ResponseEntity<GetLikedMessagesResponse> getLikedMessages(@PathVariable UUID lectureId) {
        UUID userId = getUserId();
        GetLikedMessagesInput input = new GetLikedMessagesInput(lectureId, userId);
        GetLikedMessagesOutput output = qnaChatService.getLikedMessages(input);

        List<GetLikedMessagesResponse.LikedMessageItem> messages = output.getMessages().stream()
                .map(m -> new GetLikedMessagesResponse.LikedMessageItem(
                        m.getMessageId(),
                        m.getRole(),
                        m.getContent(),
                        m.getCreatedAt(),
                        m.isLiked()
                ))
                .toList();

        GetLikedMessagesResponse response = new GetLikedMessagesResponse (
                output.getChatId(),
                messages
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "특정 QnA 채팅방에 메세지 전송",
            description = "QnA 채팅방에 질문을 하고 강의 자료를 기반으로 답변을 받습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "질문 처리 성공",
                            content = @Content(schema = @Schema(implementation = QnaChatMessageResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{" +
                                                            "\"messageId\": \"msg-550e8400-e29b-41d4-a716-446655440003\", " +
                                                            "\"role\": \"assistant\", " +
                                                            "\"content\": \"📝자료에 따르면, 재귀 함수는 자기 자신을 호출하는 함수를 의미합니다. 함수 내부에서 자신을 다시 호출하는 방식으로 동작하며, 복잡한 문제를 간단하게 해결할 수 있는 프로그래밍 기법입니다. 🤖제가 알기로는 재귀 함수는 종료 조건이 반드시 필요하며, 그렇지 않으면 무한 루프에 빠질 수 있습니다.\", " +
                                                            "\"references\": [" +
                                                            "{\"text\": \"재귀 함수(recursion)는 컴퓨터 과학에서 자기 자신을 호출하는 함수 또는 알고리즘을 말합니다. 이는 복잡한 문제를 더 작고 관리하기 쉬운 부분 문제로 나누어 해결하는 방법입니다.\", \"page\": 42}, " +
                                                            "{\"text\": \"재귀 함수는 기저 사례(base case)가 필요합니다. 기저 사례는 재귀 호출을 멈추는 조건입니다.\", \"page\": 43}" +
                                                            "{\"text\": \"재귀 함수는 함수 호출 스택을 사용합니다.\", \"page\": 20}" +
                                                            "], " +
                                                            "\"recommendedQuestions\": [\"재귀 함수의 장단점은 무엇인가요?\", \"재귀 함수와 반복문의 차이점은 무엇인가요?\", \"재귀 함수에서 기저 사례(base case)란 무엇인가요?\"], " +
                                                            "\"createdAt\": \"2025-05-28T04:15:30Z\", " +
                                                            "\"isLiked\": false" +
                                                            "}"
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
    @PostMapping(value = "/messages")
    public ResponseEntity<QnaChatMessageResponse> sendMessage(
            @PathVariable UUID lectureId,
            @RequestBody QnaChatMessageRequest request) {

        UUID userId = getUserId();
        QnaChatMessageInput input = new QnaChatMessageInput(lectureId, userId, request.getQuestion());
        QnaChatMessageOutput output = qnaChatService.ask(input);

        QnaChatMessageResponse response = new QnaChatMessageResponse(
                output.getMessageId(),
                output.getRole(),
                output.getContent(),
                output.getReferences(),
                output.getRecommendedQuestions(),
                output.getCreatedAt(),
                output.isLiked()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "채팅 메시지 좋아요 토글",
            description = "AI 응답 메시지의 좋아요를 토글합니다. 좋아요가 있으면 제거하고, 없으면 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요 토글 성공",
                            content = @Content(
                                    schema = @Schema(implementation = ToggleLikeMessageResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "좋아요 추가",
                                                    description = "좋아요가 추가된 경우",
                                                    value = "{\"isLiked\": true, \"action\": \"ADDED\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "좋아요 제거",
                                                    description = "좋아요가 제거된 경우",
                                                    value = "{\"isLiked\": false, \"action\": \"REMOVED\"}"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (사용자 메시지는 좋아요할 수 없음)",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"사용자 메시지는 좋아요할 수 없습니다\", \"code\": \"BAD_REQUEST\", \"timestamp\": \"2025-05-28T04:10:00Z\"}"
                                    )
                            })
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "메시지 또는 채팅방을 찾을 수 없음",
                            content = @Content(examples = {
                                    @ExampleObject(
                                            name = "example",
                                            value = "{\"message\": \"메시지를 찾을 수 없습니다\", \"code\": \"NOT_FOUND\", \"timestamp\": \"2025-05-28T04:10:00Z\"}"
                                    )
                            })
                    )
            }
    )
    @PostMapping("/messages/{messageId}/toggle-like")
    public ResponseEntity<ToggleLikeMessageResponse> toggleLikeMessage(
            @PathVariable UUID lectureId,
            @PathVariable UUID messageId) {
        UUID userId = getUserId();
        ToggleLikeMessageInput input = new ToggleLikeMessageInput(lectureId, messageId, userId);
        ToggleLikeMessageOutput output = qnaChatService.toggleLikeMessage(input);

        ToggleLikeMessageResponse response = new ToggleLikeMessageResponse(
                output.isLiked(),
                output.getAction()
        );
        return ResponseEntity.ok(response);
    }
}