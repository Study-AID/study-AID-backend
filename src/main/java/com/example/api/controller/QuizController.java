package com.example.api.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.adapters.sqs.GenerateQuizMessage;
import com.example.api.adapters.sqs.SQSClient;
import com.example.api.controller.dto.quiz.*;
import com.example.api.entity.enums.Status;
import com.example.api.service.CourseService;
import com.example.api.service.LectureService;
import com.example.api.service.QuizService;
import com.example.api.service.dto.quiz.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/v1/quizzes")
@Tag(name = "quiz", description = "Quiz API")
@Slf4j
public class QuizController extends BaseController {
    private final QuizService quizService;
    private final LectureService lectureService;
    private final CourseService courseService;
    private final SQSClient sqsClient;

    public QuizController(
            QuizService quizService, 
            LectureService lectureService, 
            CourseService courseService,
            SQSClient sqsClient
    ) {
        this.quizService = quizService;
        this.lectureService = lectureService;
        this.courseService = courseService;
        this.sqsClient = sqsClient;
    }
    
    @GetMapping("/lecture/{lectureId}")
    @Operation(
            summary = "Get all quizzes by lecture ID",
            description = "Retrieve quizzes associated with a specific lecture ID.",
            parameters = {
                    @Parameter(
                        name = "lectureId", 
                        description = "Lecture ID", 
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Quizzes retrieved successfully",
                        content = @Content(schema = @Schema(implementation = QuizListResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Lecture not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizListResponse> getQuizzesByLecture(
            @PathVariable UUID lectureId
    ) {
        UUID userId = getAuthenticatedUserId();
        
        // Check if the lecture exists
        var lectureOutput = lectureService.findLectureById(lectureId);
        if (lectureOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if the user is same as the quiz owner
        if (!lectureOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Retrieve quizzes associated with the lecture
        QuizListOutput quizListOutput = quizService.findQuizzesByLectureId(lectureId);

        List<QuizResponse> quizListResponse = quizListOutput.getQuizzes().stream()
                .map(quiz -> QuizResponse.fromServiceDto(quiz))
                .toList();

        return ResponseEntity.ok(new QuizListResponse(quizListResponse));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get quiz by ID",
            description = "Retrieve a specific quiz by its ID.",
            parameters = {
                    @Parameter(
                        name = "id", 
                        description = "Quiz ID", 
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Quiz retrieved successfully",
                        content = @Content(schema = @Schema(implementation = QuizResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Quiz not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizResponse> getQuizById(
            @PathVariable UUID id
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the quiz exists
            Optional<QuizOutput> quizOutput = quizService.findQuizById(id);
            if (quizOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if the user is same as the quiz owner
            if (!quizOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Convert to response DTO
            return ResponseEntity.ok(QuizResponse.fromServiceDto(quizOutput.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }        
    }
    
    private void sendGenerateQuizMessage(UUID userId, UUID courseId, QuizOutput quizOutput, int trueOrFalseCount, int multipleChoiceCount, int shortAnswerCount, int essayCount) {
        GenerateQuizMessage message = GenerateQuizMessage.builder()
                .schemaVersion("1.0.0")
                .requestId(UUID.randomUUID())
                .occurredAt(OffsetDateTime.now())
                .userId(userId)
                .courseId(courseId)
                .lectureId(quizOutput.getLectureId())
                .quizId(quizOutput.getId())
                .title(quizOutput.getTitle())
                .trueOrFalseCount(trueOrFalseCount)
                .multipleChoiceCount(multipleChoiceCount)
                .shortAnswerCount(shortAnswerCount)
                .essayCount(essayCount)
                .build();
        try {
            sqsClient.sendGenerateQuizMessage(message);
            log.info("Successfully sent generate quiz message to SQS: {}", quizOutput.getId());
        } catch (Exception e) {
            log.error("Failed to send generate quiz message to SQS: {}", e.getMessage());
        }
    }
    
    @PostMapping
    @Operation(
            summary = "Create a new quiz",
            description = "Create a new quiz associated with a specific lecture.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Quiz details (lectureId, title, status)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateQuizRequest.class))
            ),
            responses = {
                    @ApiResponse(
                        responseCode = "201", 
                        description = "Quiz created successfully",
                        content = @Content(schema = @Schema(implementation = QuizResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Lecture not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizResponse> createQuiz(
        @org.springframework.web.bind.annotation.RequestBody CreateQuizRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the lecture exists
            var lectureOutput = lectureService.findLectureById(request.getLectureId());
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if the user is same as the quiz owner
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate the request
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // Validate the counts (0 or more)
            if (request.getTrueOrFalseCount() < 0 || request.getMultipleChoiceCount() < 0 ||
                request.getShortAnswerCount() < 0 || request.getEssayCount() < 0) {
                return ResponseEntity.badRequest().build();
            }

            // Create a new quiz
            CreateQuizInput createQuizInput = new CreateQuizInput();
            createQuizInput.setLectureId(request.getLectureId());
            createQuizInput.setUserId(userId);
            createQuizInput.setTitle(request.getTitle());

            QuizOutput createdQuizOutput = quizService.createQuiz(createQuizInput);

            // Get the courseId from the lecture
            UUID courseId = lectureOutput.get().getCourseId();

            // Send a message to SQS for quiz generation
            sendGenerateQuizMessage(
                    userId, 
                    courseId, 
                    createdQuizOutput, 
                    request.getTrueOrFalseCount(), 
                    request.getMultipleChoiceCount(), 
                    request.getShortAnswerCount(), 
                    request.getEssayCount()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(QuizResponse.fromServiceDto(createdQuizOutput));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a quiz",
            description = "Update an existing quiz by its ID.",
            parameters = {
                    @Parameter(
                        name = "id", 
                        description = "Quiz ID", 
                        required = true
                    )
            },
            requestBody = @RequestBody(
                    description = "Updated quiz details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateQuizRequest.class))
            ),
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Quiz updated successfully",
                        content = @Content(schema = @Schema(implementation = QuizResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Quiz not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestBody UpdateQuizRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // TODO: 향후 개선 - Service 레이어에서 entity를 직접 반환하여 재사용하는 방식으로 변경 예정
            // 현재는 Controller에서 find하고 Service에서도 find하여 중복 조회가 발생하는 비효율적인 구조
            // Service DTO 구조 변경이 필요하여 일단 현재 구조 유지
            // Check if the quiz exists
            Optional<QuizOutput> quizOutput = quizService.findQuizById(id);
            if (quizOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if the user is same as the quiz owner
            if (!quizOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            UpdateQuizInput updateQuizInput = new UpdateQuizInput();
            updateQuizInput.setId(id);
            updateQuizInput.setTitle(request.getTitle());

            // Update the quiz
            QuizOutput updatedQuizOutput = quizService.updateQuiz(updateQuizInput);
            QuizResponse quizResponse = QuizResponse.fromServiceDto(updatedQuizOutput);

            return ResponseEntity.ok(quizResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a quiz",
            description = "Delete a specific quiz by its ID.",
            parameters = {
                    @Parameter(
                        name = "id", 
                        description = "Quiz ID", 
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "204", 
                        description = "Quiz deleted successfully"
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Quiz not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable UUID id
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the quiz exists
            Optional<QuizOutput> quizOutput = quizService.findQuizById(id);
            if (quizOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if the user is same as the quiz owner
            if (!quizOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Delete the quiz
            quizService.deleteQuiz(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 풀이 제출 API
    @PostMapping("/{id}/submit")
    @Operation(
            summary = "Submit a quiz solution",
            description = "Submit a solution for a specific quiz by its ID.",
            parameters = {
                    @Parameter(
                        name = "id", 
                        description = "Quiz ID", 
                        required = true
                    )
            },
            requestBody = @RequestBody(
                    description = "Quiz submission details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SubmitQuizRequest.class))
            ),
            responses = {
                    @ApiResponse(
                        responseCode = "201", 
                        description = "Quiz solution submitted successfully",
                        content = @Content(schema = @Schema(implementation = SubmitQuizListResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Quiz not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<SubmitQuizListResponse> submitQuiz(
            @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestBody SubmitQuizRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the quiz exists
            Optional<QuizOutput> quizOutput = quizService.findQuizById(id);
            if (quizOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if the user is same as the quiz owner
            if (!quizOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Submit the quiz solution
            List<CreateQuizResponseInput> createQuizResponseListInput = request.getSubmitQuizItems().stream()
                    .map(submitQuizItem -> {
                        CreateQuizResponseInput input = new CreateQuizResponseInput();
                        input.setQuizId(id);
                        input.setUserId(userId);
                        input.setQuizItemId(submitQuizItem.getQuizItemId());
                        input.setSelectedBool(submitQuizItem.getSelectedBool());
                        input.setSelectedIndices(submitQuizItem.getSelectedIndices());
                        input.setTextAnswer(submitQuizItem.getTextAnswer());
                        return input;
                    })
                    .toList();

            // submitAndGradeQuizWithStatus 문제 제출 및 채점, 상태관리까지
            QuizResponseListOutput quizResponseListOutput = quizService.submitAndGradeQuizWithStatus(createQuizResponseListInput);

            // quizResponseOutputs의 각 quizResponseOutput을 SubmitQuizResponse 변환하여 정상 처리되었는지 status를 확인
            List<SubmitQuizResponse> submitQuizListResponse = quizResponseListOutput.getQuizResponseOutputs().stream()
                    .map(quizResponse -> SubmitQuizResponse.fromServiceDto(quizResponse))
                    .toList();
            if (submitQuizListResponse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            return ResponseEntity.ok(new SubmitQuizListResponse(submitQuizListResponse));
        } catch (IllegalArgumentException e) {
            log.error("Bad request in submitQuiz: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Internal server error in submitQuiz for quizId: {}, userId: {}", id, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/result")
    @Operation(
            summary = "Get quiz result by quiz ID",
            description = "Retrieve the result of a specific quiz by its ID.",
            parameters = {
                    @Parameter(
                        name = "id", 
                        description = "Quiz ID", 
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Quiz result retrieved successfully",
                        content = @Content(schema = @Schema(implementation = QuizResultResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "The quiz has not been graded yet"
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Quiz result not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizResultResponse> getQuizResultById(
            @PathVariable UUID id
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the quiz exists
            Optional<QuizOutput> quizOutput = quizService.findQuizById(id);
            if (quizOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if the user is same as the quiz owner
            if (!quizOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if the quiz has been graded
            if (!(quizOutput.get().getStatus() == Status.graded || quizOutput.get().getStatus() == Status.partially_graded)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Retrieve the quiz result
            Optional<QuizResultOutput> quizResultOutput = quizService.findQuizResultByQuizId(id);
            if (quizResultOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(QuizResultResponse.fromServiceDto(quizResultOutput.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/course/{courseId}/results")
    @Operation(
            summary = "Get all quiz results by course ID",
            description = "Retrieve all quiz results associated with a specific course ID.",
            parameters = {
                    @Parameter(
                        name = "courseId", 
                        description = "Course ID", 
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Quiz results retrieved successfully",
                        content = @Content(schema = @Schema(implementation = QuizResultListResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Course not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizResultListResponse> getQuizResultsByCourse(
            @PathVariable UUID courseId
    ) {
        UUID userId = getAuthenticatedUserId();

        // Check if the course exists
        var courseOutput = courseService.findCourseById(courseId);
        if (courseOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if the user is same as the quiz owner
        if (!courseOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Retrieve quiz results associated with the course
        QuizResultListOutput quizResultOutputs = quizService.findQuizResultsByCourseId(courseId);
        Float averageScore = quizService.calculateQuizAverageScore(courseId);
        QuizResultListResponse quizResultResponses = QuizResultListResponse.fromServiceDto(quizResultOutputs, averageScore);

        return ResponseEntity.ok(quizResultResponses);
    }

    @GetMapping("/lecture/{lectureId}/items/liked")
    @Operation(
            summary = "Get liked quiz items by lecture ID",
            description = "Retrieve all liked quiz items associated with a specific lecture ID.",
            parameters = {
                    @Parameter(
                        name = "lectureId", 
                        description = "Lecture ID", 
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Liked quiz items retrieved successfully",
                        content = @Content(schema = @Schema(implementation = QuizItemListResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Lecture not found"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "No liked quiz items found for this lecture"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizItemListResponse> getLikedQuizItemsByLecture(
            @PathVariable UUID lectureId
    ) {
        UUID userId = getAuthenticatedUserId();

        // Check if the lecture exists
        var lectureOutput = lectureService.findLectureById(lectureId);
        if (lectureOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if the user is same as the quiz owner
        if (!lectureOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Retrieve liked quiz items associated with the lecture
        QuizItemListOutput quizItemListOutput = quizService.findLikedQuizItemByLectureId(lectureId);

        if (quizItemListOutput.getQuizItems().isEmpty() || quizItemListOutput == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new QuizItemListResponse(List.of()));
        }

        List<QuizItemResponse> quizItemListResponse = quizItemListOutput.getQuizItems().stream()
                .map(quizItem -> QuizItemResponse.fromServiceDto(quizItem))
                .toList();

        return ResponseEntity.ok(new QuizItemListResponse(quizItemListResponse));
    }

    @PostMapping("/{id}/items/{quizItemId}/toggle-like")
    @Operation(
            summary = "Toggle like for quiz item",
            description = "Toggle like status for a specific quiz item. If already liked, removes the like. If not liked, adds a like.",
            parameters = {
                    @Parameter(
                        name = "id", 
                        description = "Quiz ID", 
                        required = true
                    ),
                    @Parameter(
                        name = "quizItemId", 
                        description = "Quiz Item ID", 
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Like status toggled successfully",
                        content = @Content(schema = @Schema(implementation = QuizItemResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request parameters"
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Quiz or Quiz Item not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<QuizItemResponse> toggleLikeQuizItem(
            @PathVariable UUID id,
            @PathVariable UUID quizItemId
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // 퀴즈 존재 여부 확인
            Optional<QuizOutput> quizOutput = quizService.findQuizById(id);
            if (quizOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 사용자가 퀴즈 소유자인지 확인
            if (!quizOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 좋아요 토글 서비스 호출
            ToggleLikeQuizItemInput input = new ToggleLikeQuizItemInput();
            input.setQuizId(id);
            input.setQuizItemId(quizItemId);
            input.setUserId(userId);

            QuizItemOutput output = quizService.toggleLikeQuizItem(input);

            // 응답 DTO 변환
            QuizItemResponse response = QuizItemResponse.fromServiceDto(output);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
