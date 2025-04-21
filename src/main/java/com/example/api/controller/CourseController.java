package com.example.api.controller;

import com.example.api.controller.dto.course.*;
import com.example.api.service.CourseService;
import com.example.api.service.SemesterService;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.course.CreateCourseInput;
import com.example.api.service.dto.course.UpdateCourseGradesInput;
import com.example.api.service.dto.course.UpdateCourseInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import java.util.stream.Collectors;

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

    @GetMapping
    @Operation(
            summary = "Get all courses for the user",
            description = "Retrieves a list of all courses for a user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved courses",
                            content = @Content(schema = @Schema(implementation = ListCourseResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<ListCourseResponse> getCourses() {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        List<CourseOutput> courses = courseService.findCoursesByUserId(userId);
        if (courses.isEmpty()) {
            return ResponseEntity.ok(new ListCourseResponse(List.of()));
        }
        // Ownership check.
        if (!courses.get(0).getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CourseResponse> courseDtos = courses.stream()
                .map(this::convertToControllerDto)
                .collect(Collectors.toList());

        ListCourseResponse response = new ListCourseResponse(courseDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/semester/{semesterId}")
    @Operation(
            summary = "Get all courses for a specific semester",
            description = "Retrieves a list of all courses for a specific semester",
            parameters = {
                    @Parameter(
                            name = "semesterId",
                            description = "UUID of the semester",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved courses",
                            content = @Content(schema = @Schema(implementation = ListCourseResponse.class))
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
    public ResponseEntity<ListCourseResponse> getCoursesBySemester(@PathVariable UUID semesterId) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        // Check if semester exists and user owns it.
        var semesterOpt = semesterService.findSemesterById(semesterId);
        if (semesterOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!semesterOpt.get().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CourseOutput> courses = courseService.findCoursesBySemesterId(semesterId);
        List<CourseResponse> courseDtos = courses.stream()
                .map(this::convertToControllerDto)
                .collect(Collectors.toList());

        ListCourseResponse response = new ListCourseResponse(courseDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a specific course by ID",
            description = "Retrieve specific course by id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID of the course to retrieve",
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
            Optional<CourseOutput> course = courseService.findCourseById(id);
            if (course.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!course.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            CourseResponse responseDto = convertToControllerDto(course.get());
            return ResponseEntity.ok(responseDto);
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
                    content = @Content(schema = @Schema(implementation = CreateCourseInput.class))
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
            var semesterOpt = semesterService.findSemesterById(request.getSemesterId());
            if (semesterOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!semesterOpt.get().getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate name
            if (request.getName() == null || request.getName().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // Convert controller DTO to service DTO
            CreateCourseInput serviceInput = new CreateCourseInput();
            serviceInput.setUserId(userId);
            serviceInput.setSemesterId(request.getSemesterId());
            serviceInput.setName(request.getName());

            CourseOutput createdCourse = courseService.createCourse(serviceInput);

            CourseResponse responseDto = convertToControllerDto(createdCourse);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
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
                            description = "UUID of the course to update",
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
            Optional<CourseOutput> existingCourse = courseService.findCourseById(id);
            if (existingCourse.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingCourse.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Convert controller DTO to service DTO
            UpdateCourseInput serviceInput = new UpdateCourseInput();
            serviceInput.setId(id);
            serviceInput.setName(request.getName());

            CourseOutput updatedCourse = courseService.updateCourse(serviceInput);

            CourseResponse responseDto = convertToControllerDto(updatedCourse);
            return ResponseEntity.ok(responseDto);
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
                            description = "UUID of the course to update grades for",
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
            Optional<CourseOutput> existingCourse = courseService.findCourseById(id);
            if (existingCourse.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingCourse.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Convert controller DTO to service DTO
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
                            description = "UUID of the course to delete",
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
            Optional<CourseOutput> existingCourse = courseService.findCourseById(id);
            if (existingCourse.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingCourse.get().getUserId().equals(userId)) {
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

    // Helper method to convert service DTO to controller DTO
    private CourseResponse convertToControllerDto(CourseOutput courseDto) {
        CourseResponse response = new CourseResponse();
        response.setId(courseDto.getId());
        response.setUserId(courseDto.getUserId());
        response.setSemesterId(courseDto.getSemesterId());
        response.setName(courseDto.getName());
        response.setTargetGrade(courseDto.getTargetGrade());
        response.setEarnedGrade(courseDto.getEarnedGrade());
        response.setCompletedCredits(courseDto.getCompletedCredits());
        response.setCreatedAt(courseDto.getCreatedAt());
        response.setUpdatedAt(courseDto.getUpdatedAt());
        return response;
    }
}