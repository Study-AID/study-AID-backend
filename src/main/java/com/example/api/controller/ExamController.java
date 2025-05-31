package com.example.api.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.adapters.sqs.GenerateExamMessage;
import com.example.api.adapters.sqs.SQSClient;
import com.example.api.controller.dto.exam.*;
import com.example.api.service.CourseService;
import com.example.api.service.ExamService;
import com.example.api.service.dto.exam.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;





@RestController
@RequestMapping("/v1/exams")
@Tag(name = "Exam", description = "Exam API")
@Slf4j
public class ExamController extends BaseController {
    private final ExamService examService;
    private final CourseService courseService;
    private final SQSClient sqsClient;

    public ExamController(
            ExamService examService,
            CourseService courseService,
            SQSClient sqsClient
    ) {
        this.examService = examService;
        this.courseService = courseService;
        this.sqsClient = sqsClient;
    }

    
    @GetMapping("/course/{courseId}")
    @Operation(
            summary = "Get exams by course ID", 
            description = "Retrieve a list of exams associated with a specific course ID.",
            parameters = {
                    @Parameter(
                        name = "courseId", 
                        description = "ID of the course to retrieve exams for",
                        required = true
                        )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Exams found", 
                        content = @Content(schema = @Schema(implementation = ExamListOutput.class))),
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
    })
    public ResponseEntity<ExamListResponse> getExamsByCourseId(
            @PathVariable UUID courseId
    ) {
        UUID userId = getAuthenticatedUserId();

        // Check if the course exists and if the user has access to it
        var courseOutput = courseService.findCourseById(courseId);
        if (courseOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!courseOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Retrieve the exams associated with the course
        ExamListOutput examListOutput = examService.findExamsByCourseId(courseId);

        List<ExamResponse> examListResponse = examListOutput.getExams().stream()
                .map(exam -> ExamResponse.fromServiceDto(exam))
                .toList();

        return ResponseEntity.ok(new ExamListResponse(examListResponse));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get exam by ID", 
            description = "Retrieve a specific exam by its ID.",
            parameters = {
                    @Parameter(
                        name = "examId", 
                        description = "ID of the exam to retrieve",
                        required = true
                        )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Exam found", 
                        content = @Content(schema = @Schema(implementation = ExamOutput.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Exam not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
    })
    public ResponseEntity<ExamResponse> getExamById(@PathVariable UUID id
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the exam exists and if the user has access to it
            var examOutput = examService.findExamById(id);
            if (examOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!examOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Retrieve the exam associated with the ID
            return ResponseEntity.ok(ExamResponse.fromServiceDto(examOutput.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving exam with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void sendGenerateExamMessage(UUID userId, ExamOutput examOutput, int trueOrFalseCount, int multipleChoiceCount, int shortAnswerCount, int essayCount) {
        GenerateExamMessage message = GenerateExamMessage.builder()
                .schemaVersion("1.0.0")
                .requestId(UUID.randomUUID())
                .occurredAt(OffsetDateTime.now())
                .userId(userId)
                .courseId(examOutput.getCourseId())
                .examId(examOutput.getId())
                .title(examOutput.getTitle())
                .referencedLectures(examOutput.getReferencedLectures())
                .trueOrFalseCount(trueOrFalseCount)
                .multipleChoiceCount(multipleChoiceCount)
                .shortAnswerCount(shortAnswerCount)
                .essayCount(essayCount)
                .build();
        try {
            sqsClient.sendGenerateExamMessage(message);
            log.info("Successfully sent generate quiz message to SQS: {}", examOutput.getId());
        } catch (Exception e) {
            log.error("Failed to send generate quiz message to SQS: {}", e.getMessage());
        }
    }

    @PostMapping
    @Operation(
            summary = "Create a new exam", 
            description = "Create a new exam associated with a specific course.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Exam creation request body",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateExamInput.class))
            ),
            responses = {
                    @ApiResponse(
                        responseCode = "201", 
                        description = "Exam created successfully", 
                        content = @Content(schema = @Schema(implementation = ExamOutput.class))
                    ),
                    @ApiResponse(
                        responseCode = "400", 
                        description = "Invalid input data"
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
    public ResponseEntity<ExamResponse> createExam(
            @org.springframework.web.bind.annotation.RequestBody CreateExamRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the course exists and if the user has access to it
            var courseOutput = courseService.findCourseById(request.getCourseId());
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate the request
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            if (request.getReferencedLectures() == null || request.getReferencedLectures().length == 0) {
                return ResponseEntity.badRequest().build();
            }

            // Validate the counts (0 or more)
            if (request.getTrueOrFalseCount() < 0 || request.getMultipleChoiceCount() < 0 ||
                request.getShortAnswerCount() < 0 || request.getEssayCount() < 0) {
                return ResponseEntity.badRequest().build();
            }
            
            // Create the exam
            CreateExamInput createExamInput = new CreateExamInput();
            createExamInput.setUserId(userId);
            createExamInput.setCourseId(request.getCourseId());
            createExamInput.setTitle(request.getTitle());
            createExamInput.setReferencedLectures(request.getReferencedLectures());
            createExamInput.setTrueOrFalseCount(request.getTrueOrFalseCount());
            createExamInput.setMultipleChoiceCount(request.getMultipleChoiceCount());
            createExamInput.setShortAnswerCount(request.getShortAnswerCount());
            createExamInput.setEssayCount(request.getEssayCount());

            ExamOutput createdExamOutput = examService.createExam(createExamInput);
            if (createdExamOutput == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            sendGenerateExamMessage(
                    userId,
                    createdExamOutput,
                    request.getTrueOrFalseCount(),
                    request.getMultipleChoiceCount(),
                    request.getShortAnswerCount(),
                    request.getEssayCount()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(ExamResponse.fromServiceDto(createdExamOutput));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating exam", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing exam", 
            description = "Update an existing exam by its ID.",
            parameters = {
                    @Parameter(
                        name = "examId", 
                        description = "ID of the exam to update",
                        required = true
                        )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Exam update request body",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateExamRequest.class))
            ),
            responses = {
                    @ApiResponse(
                        responseCode = "200", 
                        description = "Exam updated successfully", 
                        content = @Content(schema = @Schema(implementation = ExamResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "400", 
                        description = "Invalid input data"
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Exam not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<ExamResponse> updateExam(
            @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestBody UpdateExamRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the exam exists and if the user has access to it
            var examOutput = examService.findExamById(id);
            if (examOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!examOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate the request
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // Update the exam
            UpdateExamInput updateExamInput = new UpdateExamInput();
            updateExamInput.setId(id);
            updateExamInput.setTitle(request.getTitle());

            ExamOutput updatedExamOutput = examService.updateExam(updateExamInput);
            if (updatedExamOutput == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            return ResponseEntity.ok(ExamResponse.fromServiceDto(updatedExamOutput));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating exam with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an exam", 
            description = "Delete an exam by its ID.",
            parameters = {
                    @Parameter(
                        name = "examId", 
                        description = "ID of the exam to delete",
                        required = true
                        )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "204", 
                        description = "Exam deleted successfully"
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this quiz"
                    ),
                    @ApiResponse(
                        responseCode = "404", 
                        description = "Exam not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<Void> deleteExam(
            @PathVariable UUID id
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the exam exists and if the user has access to it
            var examOutput = examService.findExamById(id);
            if (examOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!examOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Delete the exam
            examService.deleteExam(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting exam with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 풀이 제출 API
    @PostMapping("/{id}/submit")
    @Operation(
    )
    public ResponseEntity<SubmitExamListResponse> submitExam(
            @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestBody SubmitExamRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the exam exists and if the user has access to it
            var examOutput = examService.findExamById(id);
            if (examOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!examOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Submit the exam responses
            List<CreateExamResponseInput> createExamResponseListInput = request.getSubmitExamItems().stream()
                    .map(submitExamItem ->{
                        CreateExamResponseInput input = new CreateExamResponseInput();
                        input.setExamId(id);
                        input.setExamItemId(submitExamItem.getExamItemId());
                        input.setUserId(userId);
                        input.setSelectedBool(submitExamItem.getSelectedBool());
                        input.setSelectedIndices(submitExamItem.getSelectedIndices());
                        input.setTextAnswer(submitExamItem.getTextAnswer());
                        return input;
                    }).toList();

            ExamResponseListOutput examResponseListOutput = examService.createExamResponse(createExamResponseListInput);

            List<SubmitExamResponse> submitExamListResponse = examResponseListOutput.getExamResponseOutputs().stream()
                    .map(examResponse -> SubmitExamResponse.fromServiceDto(examResponse))
                    .toList();
            if (submitExamListResponse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // examService의 gradeExam 메서드를 호출하여 채점
            examService.gradeExam(id);

            return ResponseEntity.ok(new SubmitExamListResponse(submitExamListResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error submitting exam with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/course/{courseId}/items/liked")
    @Operation(
            summary = "Get liked exam items by course ID",
            description = "Retrieve a list of liked exam items associated with a specific course ID.",
            parameters = {
                    @Parameter(
                        name = "courseId",
                        description = "ID of the course to retrieve liked exam items for",
                        required = true
                    )
            },
            responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Liked exam items found",
                        content = @Content(schema = @Schema(implementation = ExamItemListResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this course"
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
    public ResponseEntity<ExamItemListResponse> getLikedExamItemsByCourseId(
            @PathVariable UUID courseId
    ) {
        UUID userId = getAuthenticatedUserId();

        // Check if the course exists and if the user has access to it
        var courseOutput = courseService.findCourseById(courseId);
        if (courseOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!courseOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Retrieve the liked exam items associated with the course
        ExamItemListOutput examItemListOutput = examService.findLikedExamItemByCourseId(courseId);

        List<ExamItemResponse> likedExamItems = examItemListOutput.getExamItems().stream()
                .map(ExamItemResponse::fromServiceDto)
                .toList();

        return ResponseEntity.ok(new ExamItemListResponse(likedExamItems));
    }

    @PostMapping("/{id}/items/{examItemId}/toggle-like")
    @Operation(
            summary = "Toggle like for exam item",
            description = "Toggle like status for a specific exam item. If already liked, removes the like. If not liked, adds a like.",
            parameters = {
                    @Parameter(
                        name = "id",
                        description = "ID of the exam",
                        required = true
                    ),
                    @Parameter(
                        name = "examItemId",
                        description = "ID of the exam item to toggle like for",
                        required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Toggle like request (empty body)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ToggleLikeExamItemRequest.class))
            ),
            responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Like toggled successfully",
                        content = @Content(schema = @Schema(implementation = ExamItemResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "User does not have access to this exam"
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "Exam or exam item not found"
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<ExamItemResponse> toggleLikeExamItem(
            @PathVariable UUID id,
            @PathVariable UUID examItemId,
            @org.springframework.web.bind.annotation.RequestBody ToggleLikeExamItemRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the exam exists and if the user has access to it
            Optional<ExamOutput> examOutput = examService.findExamById(id);
            if (examOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!examOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
            input.setExamId(id);
            input.setExamItemId(examItemId);
            input.setUserId(userId);
            
            ExamItemOutput output = examService.toggleLikeExamItem(input);

            ExamItemResponse response = ExamItemResponse.fromServiceDto(output);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error toggling like for exam item with ID: " + examItemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
