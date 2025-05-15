package com.example.api.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.dto.quiz.CreateQuizRequest;
import com.example.api.controller.dto.quiz.QuizListResponse;
import com.example.api.controller.dto.quiz.QuizResponse;
import com.example.api.controller.dto.quiz.UpdateQuizRequest;
import com.example.api.service.LectureService;
import com.example.api.service.QuizService;
import com.example.api.service.dto.quiz.CreateQuizInput;
import com.example.api.service.dto.quiz.QuizListOutput;
import com.example.api.service.dto.quiz.QuizOutput;
import com.example.api.service.dto.quiz.UpdateQuizInput;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;

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
public class QuizController {
    private QuizService quizService;
    private LectureService lectureService;

    // TODO(yoon): remove this placeholder user ID and use actual authenticated user ID.
    private final UUID PLACEHOLDER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    public QuizController(QuizService quizService, LectureService lectureService) {
        this.quizService = quizService;
        this.lectureService = lectureService;
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
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;
        
        // Check if the lecture exists
        System.out.println("Lecture ID: " + lectureId);
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
        // Quiz ID에 해당하는 Quiz와 QuizItem을 조회합니다.

        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
            QuizResponse quizResponse = QuizResponse.fromServiceDto(quizOutput.get());
            return ResponseEntity.ok(quizResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }        
    }
    
    @PostMapping
    @Operation(
            summary = "Create a new quiz",
            description = "Create a new quiz associated with a specific lecture.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Quiz details (lectureId, title, status)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateQuizInput.class))
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
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check if the lecture exists
            System.out.println("Lecture ID: " + request);
            var lectureOutput = lectureService.findLectureById(request.getLectureId());
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if the user is same as the quiz owner
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Create a new quiz
            CreateQuizInput createQuizInput = new CreateQuizInput();
            createQuizInput.setLectureId(request.getLectureId());
            createQuizInput.setUserId(userId);
            createQuizInput.setTitle(request.getTitle());
            createQuizInput.setStatus(request.getStatus());

            QuizOutput createdQuizOutput = quizService.createQuiz(createQuizInput);
            QuizResponse quizResponse = QuizResponse.fromServiceDto(createdQuizOutput);

            return ResponseEntity.status(HttpStatus.CREATED).body(quizResponse);
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
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))
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
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
            UpdateQuizInput updateQuizInput = new UpdateQuizInput();
            updateQuizInput.setId(id);
            updateQuizInput.setTitle(request.getTitle());
            updateQuizInput.setStatus(request.getStatus());

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
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
}
