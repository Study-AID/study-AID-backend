package com.example.api.controller;

import com.example.api.controller.dto.course.*;
import com.example.api.service.CourseService;
import com.example.api.service.SemesterService;
import com.example.api.service.dto.course.*;
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
@RequestMapping("/v1/courses")
@Tag(name = "Course", description = "Course API")
public class CourseController {
    private final CourseService courseService;
    private final SemesterService semesterService;

    // TODO(mj): remove this placeholder user ID and use actual authenticated user ID.
    private final UUID PLACEHOLDER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    public CourseController(CourseService courseService, SemesterService semesterService) {
        this.courseService = courseService;
        this.semesterService = semesterService;
    }

    @GetMapping("/semester/{semesterId}")
    @Operation(
            summary = "Get all courses for a specific semester",
            description = "Retrieves a list of all courses for a specific semester",
            parameters = {
                    @Parameter(
                            name = "semesterId",
                            description = "ID of the semester",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved courses",
                            content = @Content(schema = @Schema(implementation = CourseListResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have access to this semester"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Semester not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<CourseListResponse> getCoursesBySemester(@PathVariable UUID semesterId) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        // Check if semester exists and user owns it.
        var semesterOutput = semesterService.findSemesterById(semesterId);
        if (semesterOutput.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!semesterOutput.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CourseListOutput courseListOutput = courseService.findCoursesBySemesterId(semesterId);
        List<CourseResponse> courseListResponse = courseListOutput.getCourses().stream().map(
                CourseResponse::fromServiceDto).toList();
        return ResponseEntity.ok(new CourseListResponse(courseListResponse));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a specific course by ID",
            description = "Retrieve specific course by id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the course to retrieve",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved course",
                            content = @Content(schema = @Schema(implementation = CourseResponse.class))
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
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable UUID id) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check course exists and user owns it.
            Optional<CourseOutput> courseOutput = courseService.findCourseById(id);
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(CourseResponse.fromServiceDto(courseOutput.get()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(
            summary = "Create a new course",
            description = "Creates a new course",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Course details (name, semesterId)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCourseRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Course created successfully",
                            content = @Content(schema = @Schema(implementation = CourseResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or course already exists for given semester"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Semester not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CreateCourseRequest request) {
        // TODO(mj): Obtain authenticated user ID instead of placeholder
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate semester exists and user has access to it
            var semesterOutput = semesterService.findSemesterById(request.getSemesterId());
            if (semesterOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!semesterOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate name
            if (request.getName() == null || request.getName().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            CreateCourseInput serviceInput = new CreateCourseInput();
            serviceInput.setUserId(userId);
            serviceInput.setSemesterId(request.getSemesterId());
            serviceInput.setName(request.getName());

            CourseOutput createdCourseOutput = courseService.createCourse(serviceInput);
            CourseResponse courseResponse = CourseResponse.fromServiceDto(createdCourseOutput);
            return ResponseEntity.status(HttpStatus.CREATED).body(courseResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update basic info of a course",
            description = "Updates the name of an existing course",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the course to update",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated course details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCourseRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Course updated successfully",
                            content = @Content(schema = @Schema(implementation = CourseResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input"
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
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID id,
            @RequestBody UpdateCourseRequest request) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate name
            if (request.getName() == null || request.getName().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // Check course exists and user owns it.
            Optional<CourseOutput> existingCourseOutput = courseService.findCourseById(id);
            if (existingCourseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingCourseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            UpdateCourseInput serviceInput = new UpdateCourseInput();
            serviceInput.setId(id);
            serviceInput.setName(request.getName());

            CourseOutput updatedCourseOutput = courseService.updateCourse(serviceInput);
            CourseResponse courseResponse = CourseResponse.fromServiceDto(updatedCourseOutput);
            return ResponseEntity.ok(courseResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/grades")
    @Operation(
            summary = "Update grades of a course",
            description = "Updates the grade information for an existing course",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the course to update grades for",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated grade information",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCourseGradesRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Course grades updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid grade/credit values"
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
    public ResponseEntity<Void> updateCourseGrades(
            @PathVariable UUID id,
            @RequestBody UpdateCourseGradesRequest request) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate grade values
            if (request.getTargetGrade() < 0 || request.getTargetGrade() > 4.5 ||
                    request.getEarnedGrade() < 0 || request.getEarnedGrade() > 4.5) {
                return ResponseEntity.badRequest().build();
            }
            // Validate credits
            if (request.getCompletedCredits() < 0) {
                return ResponseEntity.badRequest().build();
            }

            // Check course exists and user owns it.
            Optional<CourseOutput> existingCourseOutput = courseService.findCourseById(id);
            if (existingCourseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingCourseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            UpdateCourseGradesInput serviceInput = new UpdateCourseGradesInput();
            serviceInput.setId(id);
            serviceInput.setTargetGrade(request.getTargetGrade());
            serviceInput.setEarnedGrade(request.getEarnedGrade());
            serviceInput.setCompletedCredits(request.getCompletedCredits());

            courseService.updateCourseGrades(serviceInput);
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
            summary = "Delete a course",
            description = "Deletes an existing course by id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the course to delete",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Course deleted successfully"
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
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check course exists and user owns it.
            Optional<CourseOutput> existingCourseOutput = courseService.findCourseById(id);
            if (existingCourseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingCourseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
