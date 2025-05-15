package com.example.api.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.controller.dto.lecture.CreateLectureRequest;
import com.example.api.controller.dto.lecture.LectureListResponse;
import com.example.api.controller.dto.lecture.LectureResponse;
import com.example.api.controller.dto.lecture.UpdateLectureDisplayOrderLexRequest;
import com.example.api.controller.dto.lecture.UpdateLectureRequest;
import com.example.api.service.CourseService;
import com.example.api.service.LectureService;
import com.example.api.service.dto.lecture.CreateLectureInput;
import com.example.api.service.dto.lecture.LectureListOutput;
import com.example.api.service.dto.lecture.LectureOutput;
import com.example.api.service.dto.lecture.UpdateLectureDisplayOrderLexInput;
import com.example.api.service.dto.lecture.UpdateLectureInput;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/lectures")
@Tag(name = "lecture", description = "Lecture API")
public class LectureController {
    private LectureService lectureService;
    private CourseService courseService;

    // TODO(yoon): remove this placeholder user ID and use actual authenticated user ID.
    private final UUID PLACEHOLDER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    public LectureController(LectureService lectureService, CourseService courseService) {
        this.lectureService = lectureService;
        this.courseService = courseService;
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
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;
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

    @PostMapping
    @Operation(
        summary = "Create a new lecture",
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
            )
        }
    )
    public ResponseEntity<LectureResponse> createLecture(@RequestBody CreateLectureRequest request) {
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        // TODO(yoon): Remove this placeholder data and use actual data from server data
        String testTitle = "Test Title";
        String testMaterialPath = "test/path";
        String testMaterialType = "pdf";
        String testDisplayOrderLex = "1";

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

            // TODO(yoon): Validate input data
            // Validate title
            // Validate materialPath
            // ...

            CreateLectureInput createLectureInput = new CreateLectureInput();
            createLectureInput.setCourseId(request.getCourseId());
            createLectureInput.setUserId(userId);
            createLectureInput.setTitle(testTitle);
            createLectureInput.setMaterialPath(testMaterialPath);
            createLectureInput.setMaterialType(testMaterialType);
            createLectureInput.setDisplayOrderLex(testDisplayOrderLex);

            LectureOutput createdlectureOutput = lectureService.createLecture(createLectureInput);
            return ResponseEntity.status(HttpStatus.CREATED).body(LectureResponse.fromServiceDto(createdlectureOutput));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
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
        @PathVariable UUID id,
        @RequestBody UpdateLectureRequest request
    ) {
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
        @PathVariable UUID id,
        @RequestBody UpdateLectureDisplayOrderLexRequest request
    ) {
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
        // TODO(yoon): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
