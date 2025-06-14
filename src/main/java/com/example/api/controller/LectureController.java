package com.example.api.controller;

import com.example.api.adapters.sqs.GenerateSummaryMessage;
import com.example.api.adapters.sqs.SQSClient;
import com.example.api.config.StorageConfig;
import com.example.api.controller.dto.lecture.*;
import com.example.api.service.CourseService;
import com.example.api.service.LectureService;
import com.example.api.service.StorageService;
import com.example.api.service.dto.lecture.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/lectures")
@Tag(name = "Lecture", description = "Lecture API")
@Slf4j
public class LectureController extends BaseController {
    private final LectureService lectureService;
    private final CourseService courseService;
    private final StorageService storageService;
    private final SQSClient sqsClient;
    private final StorageConfig storageConfig;

    public LectureController(
            LectureService lectureService, CourseService courseService, StorageService storageService, 
            SQSClient sqsClient, StorageConfig storageConfig
    ) {
        this.lectureService = lectureService;
        this.courseService = courseService;
        this.storageService = storageService;
        this.sqsClient = sqsClient;
        this.storageConfig = storageConfig;
    }

    @GetMapping("/course/{courseId}")
    @Operation(
            summary = "Get all lectures for a specific course",
            description = "Retrieves a list of all lectures for a specific course",
            parameters = {
                    @Parameter(
                            name = "courseId",
                            description = "ID of the course",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved lectures",
                            content = @Content(schema = @Schema(implementation = LectureListResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this lecture"
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
    public ResponseEntity<LectureListResponse> getLecturesByCourse(@PathVariable UUID courseId) {
        UUID userId = getAuthenticatedUserId();

        // Check if the course exists
        var courseOutput = courseService.findCourseById(courseId);
        if (courseOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Check if the user is same as the course owner
        if (!courseOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LectureListOutput lectureListOutput = lectureService.findLecturesByCourseId(courseId);
        List<LectureResponse> lectureListResponse = lectureListOutput.getLectures().stream()
                .map(lecture -> LectureResponse.fromServiceDto(lecture, storageConfig))
                .toList();
        return ResponseEntity.ok(new LectureListResponse(lectureListResponse));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a specific lecture by ID",
            description = "Retrieves a specific lecture by its ID",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the lecture to retrieve",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved lecture",
                            content = @Content(schema = @Schema(implementation = LectureResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this lecture"
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
    public ResponseEntity<LectureResponse> getLectureById(@PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the lecture exists
            Optional<LectureOutput> lectureOutput = lectureService.findLectureById(id);
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(LectureResponse.fromServiceDto(lectureOutput.get(), storageConfig));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/preview")
    @Operation(
            summary = "Get a lecture preview by ID",
            description = "Retrieves a lecture preview with keywords by its ID",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the lecture to retrieve preview for",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved lecture preview",
                            content = @Content(schema = @Schema(implementation = LecturePreviewResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this lecture"
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
    public ResponseEntity<LecturePreviewResponse> getLecturePreviewById(@PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the lecture exists
            Optional<LectureOutput> lectureOutput = lectureService.findLectureById(id);
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(LecturePreviewResponse.fromServiceDto(lectureOutput.get(), storageConfig));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineMaterialType(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        // Currently only PDF is supported
        if (contentType != null && contentType.equals("application/pdf")) {
            return "pdf";
        }

        // Fallback to filename extension
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            return "pdf";
        }

        // TODO: Add support for audio (mp3, wav) and presentation (pptx) formats in the future
        throw new IllegalArgumentException("Only PDF files are supported. Uploaded file type: " + contentType);
    }

    private String generateDisplayOrderLex() {
        // TODO: Implement proper lexicographic ordering generation
        // This is a placeholder implementation
        return String.valueOf(System.currentTimeMillis());
    }

    private void sendGenerateSummaryMessage(LectureOutput lectureOutput, String fileKey, UUID userId) {
        GenerateSummaryMessage message = GenerateSummaryMessage.builder()
                .schemaVersion("1.0.0")
                .requestId(UUID.randomUUID())
                .occurredAt(OffsetDateTime.now())
                .userId(userId)
                .courseId(lectureOutput.getCourseId())
                .lectureId(lectureOutput.getId())
                .s3Bucket(storageService.getBucket())
                .s3Key(fileKey)
                .build();

        try {
            sqsClient.sendGenerateSummaryMessage(message);
            log.info("Successfully sent generate summary message for lecture: {}", lectureOutput.getId());
        } catch (Exception e) {
            log.error("Failed to send generate summary message for lecture: {}", lectureOutput.getId(), e);
            // Don't fail the whole request if SQS message fails
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(
            description = "Creates a new lecture",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CreateLectureRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created lecture",
                            content = @Content(schema = @Schema(implementation = LectureResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    ),
            }
    )
    public ResponseEntity<LectureResponse> createLecture(@ModelAttribute CreateLectureRequest request) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the course exists
            var courseOutput = courseService.findCourseById(request.getCourseId());
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            // Check if the user is same as the course owner
            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate input data
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getFile() == null || request.getFile().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Upload file to storage
            MultipartFile file = request.getFile();

            // Validate file type
            String materialType;
            try {
                materialType = determineMaterialType(file);
            } catch (IllegalArgumentException e) {
                log.error("Invalid file type uploaded", e);
                return ResponseEntity.badRequest().build();
            }

            String fileKey = storageService.upload(file);

            CreateLectureInput createLectureInput = new CreateLectureInput();
            createLectureInput.setCourseId(request.getCourseId());
            createLectureInput.setUserId(userId);
            // TODO(mj): extract title from material.
            createLectureInput.setTitle(request.getTitle());
            createLectureInput.setMaterialPath(fileKey);
            createLectureInput.setMaterialType(materialType);
            createLectureInput.setDisplayOrderLex(generateDisplayOrderLex());

            LectureOutput createdLectureOutput = lectureService.createLecture(createLectureInput);

            // Send SQS message for summary generation
            sendGenerateSummaryMessage(createdLectureOutput, fileKey, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(LectureResponse.fromServiceDto(createdLectureOutput, storageConfig));
        } catch (IllegalArgumentException e) {
            log.error("Invalid input data", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating lecture", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a lecture",
            description = "Updates an existing lecture",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the lecture to update",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lecture details (title, materialPath, materialType)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateLectureRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated lecture",
                            content = @Content(schema = @Schema(implementation = LectureResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this lecture"
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
    public ResponseEntity<LectureResponse> updateLecture(
            @PathVariable @Parameter(name = "id", description = "ID of the lecture to update", required = true) UUID id,
            @RequestBody UpdateLectureRequest request) {
        UUID userId = getAuthenticatedUserId();

        try {
            // TODO: 향후 개선 - Service 레이어에서 entity를 직접 반환하여 재사용하는 방식으로 변경 예정
            // 현재는 Controller에서 find하고 Service에서도 find하여 중복 조회가 발생하는 비효율적인 구조
            // Service DTO 구조 변경이 필요하여 일단 현재 구조 유지
            // Check if the lecture exists
            Optional<LectureOutput> lectureOutput = lectureService.findLectureById(id);
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate title
            if (request.getTitle() == null || request.getTitle().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            UpdateLectureInput updateLectureInput = new UpdateLectureInput();
            updateLectureInput.setId(id);
            updateLectureInput.setTitle(request.getTitle());

            LectureOutput updatedLectureOutput = lectureService.updateLecture(updateLectureInput);
            LectureResponse lectureResponse = LectureResponse.fromServiceDto(updatedLectureOutput, storageConfig);
            return ResponseEntity.ok(lectureResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/display-order-lex")
    @Operation(
            summary = "Update lecture display order lex",
            description = "Updates the display order lex of a lecture",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the lecture to update",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated lecture display order lex (displayOrderLex)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateLectureDisplayOrderLexRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successfully updated lecture display order lex",
                            content = @Content(schema = @Schema(implementation = LectureResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this lecture"
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
    public ResponseEntity<Void> updateLectureDisplayOrderLex(
            @PathVariable @Parameter(name = "id", description = "ID of the lecture to update", required = true) UUID id,
            @RequestBody UpdateLectureDisplayOrderLexRequest request) {
        UUID userId = getAuthenticatedUserId();

        try {
            // TODO: 향후 개선 - Service 레이어에서 entity를 직접 반환하여 재사용하는 방식으로 변경 예정
            // 현재는 Controller에서 find하고 Service에서도 find하여 중복 조회가 발생하는 비효율적인 구조
            // Service DTO 구조 변경이 필요하여 일단 현재 구조 유지
            // Check if the lecture exists
            Optional<LectureOutput> lectureOutput = lectureService.findLectureById(id);
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate display order lex
            if (request.getDisplayOrderLex() == null || request.getDisplayOrderLex().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            UpdateLectureDisplayOrderLexInput updateLectureInput = new UpdateLectureDisplayOrderLexInput();
            updateLectureInput.setId(id);
            updateLectureInput.setDisplayOrderLex(request.getDisplayOrderLex());

            lectureService.updateLectureDisplayOrderLex(updateLectureInput);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a lecture",
            description = "Deletes a specific lecture by its ID",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the lecture to delete",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successfully deleted lecture"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this lecture"
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
    public ResponseEntity<Void> deleteLecture(@PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the lecture exists
            Optional<LectureOutput> lectureOutput = lectureService.findLectureById(id);
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            lectureService.deleteLecture(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/note")
    @Operation(
            summary = "Update lecture note",
            description = "Updates the note of a lecture",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the lecture to update",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated lecture note",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateLectureNoteRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated lecture note",
                            content = @Content(schema = @Schema(implementation = LectureResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this lecture"
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
    public ResponseEntity<LectureResponse> updateLectureNote(
            @PathVariable @Parameter(name = "id", description = "ID of the lecture to update", required = true) UUID id,
            @RequestBody UpdateLectureNoteRequest request) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the lecture exists
            Optional<LectureOutput> lectureOutput = lectureService.findLectureById(id);
            if (lectureOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!lectureOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate note
            if (request.getNote() == null || request.getNote().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            UpdateLectureNoteInput updateLectureInput = new UpdateLectureNoteInput();
            updateLectureInput.setId(id);
            // Convert String to Map<String, String>
            updateLectureInput.setNote(Map.of("content", request.getNote())); // Assuming note is a simple key-value pair

            LectureOutput updatedLectureOutput = lectureService.updateLectureNote(updateLectureInput);
            return ResponseEntity.ok(LectureResponse.fromServiceDto(updatedLectureOutput, storageConfig));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }        
}
