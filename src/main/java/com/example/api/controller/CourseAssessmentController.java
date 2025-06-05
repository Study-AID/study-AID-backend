package com.example.api.controller;

import com.example.api.controller.dto.courseAssessment.*;
import com.example.api.service.CourseAssessmentService;
import com.example.api.service.CourseService;
import com.example.api.service.dto.courseAssessment.*;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/courses/{courseId}/assessments")
@Tag(name = "Course Assessment", description = "Course Assessment API")
public class CourseAssessmentController extends BaseController {
    private final CourseAssessmentService courseAssessmentService;
    private final CourseService courseService;

    public CourseAssessmentController(
            CourseAssessmentService courseAssessmentService,
            CourseService courseService
    ) {
        this.courseAssessmentService = courseAssessmentService;
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(
            summary = "Get all course assessments for a specific course",
            description = "Retrieves a list of all course assessments for a specific course",
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
                            description = "Successfully retrieved course assessments",
                            content = @Content(schema = @Schema(implementation = CourseAssessmentListResponse.class))
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
    public ResponseEntity<CourseAssessmentListResponse> getCourseAssessmentsByCourse(@PathVariable UUID courseId) {
        UUID userId = getAuthenticatedUserId();

        // Check if the course exists and user owns it
        var courseOutput = courseService.findCourseById(courseId);
        if (courseOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!courseOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CourseAssessmentListOutput courseAssessmentListOutput = courseAssessmentService.findCourseAssessmentsByCourseId(courseId);
        List<CourseAssessmentResponse> courseAssessmentListResponse = courseAssessmentListOutput.getCourseAssessments().stream()
                .map(CourseAssessmentResponse::fromServiceDto)
                .toList();
        return ResponseEntity.ok(new CourseAssessmentListResponse(courseAssessmentListResponse));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a specific course assessment by ID",
            description = "Retrieves a specific course assessment by its ID",
            parameters = {
                    @Parameter(
                            name = "courseId",
                            description = "ID of the course",
                            required = true
                    ),
                    @Parameter(
                            name = "id",
                            description = "ID of the course assessment to retrieve",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved course assessment",
                            content = @Content(schema = @Schema(implementation = CourseAssessmentResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this course assessment"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Course assessment not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<CourseAssessmentResponse> getCourseAssessmentById(@PathVariable UUID courseId, @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the course exists and user owns it
            var courseOutput = courseService.findCourseById(courseId);
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if the course assessment exists
            Optional<CourseAssessmentOutput> courseAssessmentOutput = courseAssessmentService.findCourseAssessmentById(id);
            if (courseAssessmentOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            // Verify the course assessment belongs to the specified course
            if (!courseAssessmentOutput.get().getCourseId().equals(courseId)) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(CourseAssessmentResponse.fromServiceDto(courseAssessmentOutput.get()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(
            summary = "Create a new course assessment",
            description = "Creates a new course assessment",
            parameters = {
                    @Parameter(
                            name = "courseId",
                            description = "ID of the course",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Course assessment details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCourseAssessmentRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Course assessment created successfully",
                            content = @Content(schema = @Schema(implementation = CourseAssessmentResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
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
    public ResponseEntity<CourseAssessmentResponse> createCourseAssessment(
            @PathVariable UUID courseId,
            @RequestBody CreateCourseAssessmentRequest request) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the course exists and user owns it
            var courseOutput = courseService.findCourseById(courseId);
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate input data
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getScore() == null || request.getScore() < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getMaxScore() == null || request.getMaxScore() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getScore() > request.getMaxScore()) {
                return ResponseEntity.badRequest().build();
            }

            CreateCourseAssessmentInput createInput = new CreateCourseAssessmentInput();
            createInput.setCourseId(courseId);
            createInput.setUserId(userId);
            createInput.setTitle(request.getTitle());
            createInput.setScore(request.getScore());
            createInput.setMaxScore(request.getMaxScore());

            CourseAssessmentOutput createdOutput = courseAssessmentService.createCourseAssessment(createInput);
            return ResponseEntity.status(HttpStatus.CREATED).body(CourseAssessmentResponse.fromServiceDto(createdOutput));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a course assessment",
            description = "Updates an existing course assessment",
            parameters = {
                    @Parameter(
                            name = "courseId",
                            description = "ID of the course",
                            required = true
                    ),
                    @Parameter(
                            name = "id",
                            description = "ID of the course assessment to update",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated course assessment details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCourseAssessmentRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Course assessment updated successfully",
                            content = @Content(schema = @Schema(implementation = CourseAssessmentResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this course assessment"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Course assessment not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<CourseAssessmentResponse> updateCourseAssessment(
            @PathVariable UUID courseId,
            @PathVariable UUID id,
            @RequestBody UpdateCourseAssessmentRequest request) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the course exists and user owns it
            var courseOutput = courseService.findCourseById(courseId);
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if the course assessment exists and belongs to the course
            Optional<CourseAssessmentOutput> existingOutput = courseAssessmentService.findCourseAssessmentById(id);
            if (existingOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingOutput.get().getCourseId().equals(courseId)) {
                return ResponseEntity.notFound().build();
            }

            // Validate input data
            if (request.getTitle() == null && request.getScore() == null && request.getMaxScore() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // If title, score, or maxScore is provided, validate them
            if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getScore() != null && request.getScore() < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getMaxScore() != null && request.getMaxScore() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getScore() > request.getMaxScore()) {
                return ResponseEntity.badRequest().build();
            }

            // If any field is null, use the existing value
            if (request.getTitle() == null) {
                request.setTitle(existingOutput.get().getTitle());
            }
            if (request.getScore() == null) {
                request.setScore(existingOutput.get().getScore());
            }
            if (request.getMaxScore() == null) {
                request.setMaxScore(existingOutput.get().getMaxScore());
            }

            UpdateCourseAssessmentInput updateInput = new UpdateCourseAssessmentInput();
            updateInput.setId(id);
            updateInput.setTitle(request.getTitle());
            updateInput.setScore(request.getScore());
            updateInput.setMaxScore(request.getMaxScore());

            CourseAssessmentOutput updatedOutput = courseAssessmentService.updateCourseAssessment(updateInput);
            return ResponseEntity.ok(CourseAssessmentResponse.fromServiceDto(updatedOutput));
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
            summary = "Delete a course assessment",
            description = "Deletes a specific course assessment by its ID",
            parameters = {
                    @Parameter(
                            name = "courseId",
                            description = "ID of the course",
                            required = true
                    ),
                    @Parameter(
                            name = "id",
                            description = "ID of the course assessment to delete",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Course assessment deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this course assessment"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Course assessment not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<Void> deleteCourseAssessment(@PathVariable UUID courseId, @PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Check if the course exists and user owns it
            var courseOutput = courseService.findCourseById(courseId);
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if the course assessment exists and belongs to the course
            Optional<CourseAssessmentOutput> existingOutput = courseAssessmentService.findCourseAssessmentById(id);
            if (existingOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingOutput.get().getCourseId().equals(courseId)) {
                return ResponseEntity.notFound().build();
            }

            courseAssessmentService.deleteCourseAssessment(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}