package com.example.api.controller;

import com.example.api.adapters.sqs.GenerateSummaryMessage;
import com.example.api.adapters.sqs.SQSClient;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/lectures")
@Tag(name = "lecture", description = "Lecture API")
@Slf4j
public class LectureController {
    private final LectureService lectureService;
    private final CourseService courseService;
    private final StorageService storageService;
    private final SQSClient sqsClient;

    public LectureController(
            LectureService lectureService, CourseService courseService, StorageService storageService, SQSClient sqsClient
    ) {
        this.lectureService = lectureService;
        this.courseService = courseService;
        this.storageService = storageService;
        this.sqsClient = sqsClient;
    }

    @GetMapping("/course/{courseId}")
    @Operation(
            summary = "Get all lectures for a specific course",
            description = "Retrieves a list of all lectures for a specific course",
            parameters = {
                    @Parameter(
                            name = "courseId",
                            description = "ID of the lecture",
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
        List<LectureResponse> lectureListResponse = lectureListOutput.getLectures().stream().map(LectureResponse::fromServiceDto).toList();
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
            return ResponseEntity.ok(LectureResponse.fromServiceDto(lectureOutput.get()));
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

    @PostMapping
    @Operation(
            description = "Creates a new lecture",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lecture details (courseId, title)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateLectureRequest.class))
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
    public ResponseEntity<LectureResponse> createLecture(@RequestBody CreateLectureRequest request) {
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

            return ResponseEntity.status(HttpStatus.CREATED).body(LectureResponse.fromServiceDto(createdLectureOutput));
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
            // Validate materialPath
            if (request.getMaterialPath() == null || request.getMaterialPath().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            // Validate materialType
            if (request.getMaterialType() == null || request.getMaterialType().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            UpdateLectureInput updateLectureInput = new UpdateLectureInput();
            updateLectureInput.setId(id);
            updateLectureInput.setTitle(request.getTitle());
            updateLectureInput.setMaterialPath(request.getMaterialPath());
            updateLectureInput.setMaterialType(request.getMaterialType());

            LectureOutput updatedLectureOutput = lectureService.updateLecture(updateLectureInput);
            LectureResponse lectureResponse = LectureResponse.fromServiceDto(updatedLectureOutput);
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
}
