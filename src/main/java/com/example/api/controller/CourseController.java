package com.example.api.controller;

import com.example.api.controller.dto.course.*;
import com.example.api.entity.CourseWeaknessAnalysis;
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
public class CourseController extends BaseController {
    private final CourseService courseService;
    private final SemesterService semesterService;

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
        UUID userId = getAuthenticatedUserId();

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
        UUID userId = getAuthenticatedUserId();

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

    @GetMapping("/{id}/weakness-analysis")
    @Operation(
            summary = "Get course weakness analysis",
            description = "Retrieve weakness analysis for a specific course based on quiz and exam results",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the course to get weakness analysis for",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved weakness analysis",
                            content = @Content(
                                    schema = @Schema(implementation = CourseWeaknessAnalysis.class),
                                    examples = {
                                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                    name = "With Analysis",
                                                    description = "When weakness analysis is available",
                                                    value = """
                                                {
                                                  "weaknesses": "SQL 기본 문법에 대한 이해가 부족합니다. 특히 SELECT 문의 사용법과 WHERE 절의 개념이 명확하지 않으며, JOIN 연산에 대한 이해도 불충분합니다. 또한 데이터 타입 변환과 함수 사용에서도 실수가 자주 발생하고 있습니다.",
                                                  "suggestions": "SQL 기본 문법을 체계적으로 학습하고, 간단한 쿼리부터 차근차근 연습해보세요. JOIN 개념을 확실히 이해하기 위해 다양한 예제를 풀어보는 것을 추천합니다. 온라인 SQL 연습 사이트(예: SQLBolt, W3Schools)를 활용하여 실습 위주로 학습하시고, 특히 WHERE 절과 함수 사용법을 집중적으로 연습해보세요.",
                                                  "analyzed_at": "2024-06-11T15:30:45"
                                                }
                                                """
                                            ),
                                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                    name = "No Analysis",
                                                    description = "When no weakness analysis is available yet",
                                                    value = """
                                                {
                                                  "weaknesses": null,
                                                  "suggestions": null,
                                                  "analyzed_at": null
                                                }
                                                """
                                            )
                                    }
                            )
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
    public ResponseEntity<CourseWeaknessAnalysis> getCourseWeaknessAnalysis(@PathVariable UUID id) {
        UUID userId = getAuthenticatedUserId();

        try {
            // 과목 존재 여부 및 권한 확인
            Optional<CourseOutput> courseOutput = courseService.findCourseById(id);
            if (courseOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!courseOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 약점 분석 조회
            CourseWeaknessAnalysis weaknessAnalysis = courseService.findCourseWeaknessAnalysis(id);

            // 약점 분석이 없으면 null 값들로 구성된 빈 객체 반환
            if (weaknessAnalysis == null) {
                CourseWeaknessAnalysis emptyAnalysis = new CourseWeaknessAnalysis();
                return ResponseEntity.ok(emptyAnalysis);
            }

            return ResponseEntity.ok(weaknessAnalysis);
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
        UUID userId = getAuthenticatedUserId();

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
        UUID userId = getAuthenticatedUserId();

        try {
            // Validate name
            if (request.getName() == null || request.getName().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // TODO: 향후 개선 - Service 레이어에서 entity를 직접 반환하여 재사용하는 방식으로 변경 예정
            // 현재는 Controller에서 find하고 Service에서도 find하여 중복 조회가 발생하는 비효율적인 구조
            // Service DTO 구조 변경이 필요하여 일단 현재 구조 유지
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
        UUID userId = getAuthenticatedUserId();

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

            // TODO: 향후 개선 - Service 레이어에서 entity를 직접 반환하여 재사용하는 방식으로 변경 예정
            // 현재는 Controller에서 find하고 Service에서도 find하여 중복 조회가 발생하는 비효율적인 구조
            // Service DTO 구조 변경이 필요하여 일단 현재 구조 유지
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
        UUID userId = getAuthenticatedUserId();

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
