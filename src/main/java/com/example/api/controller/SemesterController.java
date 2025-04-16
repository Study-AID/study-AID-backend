package com.example.api.controller;

import com.example.api.entity.Semester;
import com.example.api.entity.enums.Season;
import com.example.api.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/semesters")
@Tag(name = "Semester", description = "Semester API")
public class SemesterController {
    private final SemesterService semesterService;

    // TODO(mj): remove this placeholder user ID and use actual authenticated user ID.
    private final UUID PLACEHOLDER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    @Operation(
            summary = "Get all semesters for the user",
            description = "Retrieves a list of all semesters for a user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved semesters",
                            content = @Content(schema = @Schema(implementation = SemesterListResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<SemesterListResponseDto> getSemesters() {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        List<Semester> semesters = semesterService.findSemestersByUserId(userId);
        if (semesters.isEmpty()) {
            return ResponseEntity.ok(new SemesterListResponseDto(List.of()));
        }
        // Ownership check.
        if (!semesters.get(0).getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SemesterResponseDto> semesterDtos = semesters.stream()
                .map(SemesterResponseDto::fromEntity)
                .collect(Collectors.toList());

        SemesterListResponseDto response = new SemesterListResponseDto(semesterDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Find a specific semester by year and season",
            description = "Retrieves a semester with the year and season",
            parameters = {
                    @Parameter(
                            name = "year",
                            description = "year",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "2024"
                    ),
                    @Parameter(
                            name = "season",
                            description = "season (spring, summer, fall, winter)",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "FALL")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved semester",
                            content = @Content(schema = @Schema(implementation = SemesterResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input (e.g., invalid year or season)"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Unauthorized access"
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
    public ResponseEntity<SemesterResponseDto> getSemesterByYearAndSeason(
            @RequestParam int year,
            @RequestParam Season season) {
        // TODO(mj): Obtain authenticated user ID instead of placeholder
        UUID userId = PLACEHOLDER_USER_ID;

        if (year < 0) {
            return ResponseEntity.badRequest().build();
        }
        if (season == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!Arrays.asList(Season.values()).contains(season)) {
            return ResponseEntity.badRequest().build();
        }

        // Check semester exists and user owns it.
        Optional<Semester> semester = semesterService.findSemesterByUserAndYearAndSeason(userId, year, season);
        if (semester.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!semester.get().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SemesterResponseDto responseDto = SemesterResponseDto.fromEntity(semester.get());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a specific semester by ID",
            description = "Retrieve specific semester by id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID of the semester to retrieve",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved semester",
                            content = @Content(schema = @Schema(implementation = SemesterResponseDto.class))
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
    public ResponseEntity<SemesterResponseDto> getSemesterById(@PathVariable UUID id) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check semester exists and user owns it.
            Optional<Semester> semester = semesterService.findSemesterById(id);
            if (semester.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!semester.get().getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            SemesterResponseDto responseDto = SemesterResponseDto.fromEntity(semester.get());
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(
            summary = "Create a new semester",
            description = "Creates a new semester",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Semester details (name, year, season)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SemesterCreateDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Semester created successfully",
                            content = @Content(schema = @Schema(implementation = SemesterResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or semester already exists for given year & season"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<SemesterResponseDto> createSemester(
            @RequestBody SemesterCreateDto semesterDto) {
        // TODO(mj): Obtain authenticated user ID instead of placeholder
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate year
            if (semesterDto.getYear() < 0) {
                return ResponseEntity.badRequest().build();
            }
            // Check if the season is valid
            if (semesterDto.getSeason() == null) {
                return ResponseEntity.badRequest().build();
            }
            if (!Arrays.asList(Season.values()).contains(semesterDto.getSeason())) {
                return ResponseEntity.badRequest().build();
            }

            Semester createdSemester = semesterService.createSemester(
                    userId,
                    semesterDto.getName(),
                    semesterDto.getYear(),
                    semesterDto.getSeason()
            );

            SemesterResponseDto responseDto = SemesterResponseDto.fromEntity(createdSemester);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update basic info of a semester",
            description = "Updates the name, year, and season of an existing semester",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID of the semester to update",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated semester details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SemesterUpdateDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Semester updated successfully",
                            content = @Content(schema = @Schema(implementation = SemesterResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input"
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
    public ResponseEntity<SemesterResponseDto> updateSemester(
            @PathVariable UUID id,
            @RequestBody SemesterUpdateDto semesterDto) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate year
            if (semesterDto.getYear() < 0) {
                return ResponseEntity.badRequest().build();
            }
            // Check if the season is valid
            if (semesterDto.getSeason() == null) {
                return ResponseEntity.badRequest().build();
            }
            if (!Arrays.asList(Season.values()).contains(semesterDto.getSeason())) {
                return ResponseEntity.badRequest().build();
            }

            // Check semester exists and user owns it.
            Optional<Semester> existingSemester = semesterService.findSemesterById(id);
            if (existingSemester.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemester.get().getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Semester updatedSemester = semesterService.updateSemester(
                    id,
                    semesterDto.getName(),
                    semesterDto.getYear(),
                    semesterDto.getSeason()
            );

            SemesterResponseDto responseDto = SemesterResponseDto.fromEntity(updatedSemester);
            return ResponseEntity.ok(responseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/dates")
    @Operation(
            summary = "Update dates of a semester",
            description = "Updates the start and end dates for an existing semester",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID of the semester to update dates for",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated start and end dates",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SemesterUpdateDatesDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Semester dates updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid dates (e.g., end date before start date)"
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
    public ResponseEntity<Void> updateSemesterDates(
            @PathVariable UUID id,
            @RequestBody SemesterUpdateDatesDto datesDto) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check if start and end dates are provided
            if (datesDto.getStartDate() == null || datesDto.getEndDate() == null) {
                return ResponseEntity.badRequest().build();
            }
            // Check if start date is before end date
            if (datesDto.getEndDate().isBefore(datesDto.getStartDate())) {
                return ResponseEntity.badRequest().build();
            }

            // Check semester exists and user owns it.
            Optional<Semester> existingSemester = semesterService.findSemesterById(id);
            if (existingSemester.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemester.get().getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            semesterService.updateSemesterDates(id, datesDto.getStartDate(), datesDto.getEndDate());
            return ResponseEntity.noContent().build();
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
            summary = "Update grades of a semester",
            description = "Updates the grade information for an existing semester",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID of the semester to update grades for",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated grade information",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SemesterUpdateGradesDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Semester grades updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid grade/credit values"
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
    public ResponseEntity<Void> updateSemesterGrades(
            @PathVariable UUID id,
            @RequestBody SemesterUpdateGradesDto gradesDto) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate grade values
            if (gradesDto.getTargetGrade() < 0 || gradesDto.getTargetGrade() > 4.5 ||
                    gradesDto.getEarnedGrade() < 0 || gradesDto.getEarnedGrade() > 4.5) {
                return ResponseEntity.badRequest().build();
            }
            // Validate credits
            if (gradesDto.getCompletedCredits() < 0) {
                return ResponseEntity.badRequest().build();
            }

            // Check semester exists and user owns it.
            Optional<Semester> existingSemester = semesterService.findSemesterById(id);
            if (existingSemester.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemester.get().getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            semesterService.updateSemesterGrades(
                    id,
                    gradesDto.getTargetGrade(),
                    gradesDto.getEarnedGrade(),
                    gradesDto.getCompletedCredits()
            );
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
            summary = "Delete a semester",
            description = "Deletes an existing semester by id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID of the semester to delete",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Semester deleted successfully"
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
    public ResponseEntity<Void> deleteSemester(@PathVariable UUID id) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check semester exists and user owns it.
            Optional<Semester> existingSemester = semesterService.findSemesterById(id);
            if (existingSemester.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemester.get().getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            semesterService.deleteSemester(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Semester response Dto")
    static class SemesterResponseDto {
        private UUID id;
        private UUID userId;
        private String name;
        private int year;
        private Season season;
        private LocalDate startDate;
        private LocalDate endDate;
        private Float targetGrade;
        private Float earnedGrade;
        private Integer completedCredits;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Convert from entity to DTO
        public static SemesterResponseDto fromEntity(Semester semester) {
            SemesterResponseDto dto = new SemesterResponseDto();
            dto.setId(semester.getId());
            dto.setUserId(semester.getUser() != null ? semester.getUser().getId() : null);
            dto.setName(semester.getName());
            dto.setYear(semester.getYear());
            dto.setSeason(semester.getSeason());
            dto.setStartDate(semester.getStartDate());
            dto.setEndDate(semester.getEndDate());
            dto.setTargetGrade(semester.getTargetGrade());
            dto.setEarnedGrade(semester.getEarnedGrade());
            dto.setCompletedCredits(semester.getCompletedCredits());
            dto.setCreatedAt(semester.getCreatedAt() != null ? semester.getCreatedAt() : null);
            dto.setUpdatedAt(semester.getUpdatedAt() != null ? semester.getUpdatedAt() : null);
            return dto;
        }
    }

    @Data
    @NoArgsConstructor
    static class SemesterCreateDto {
        private String name;
        private int year;
        private Season season;
    }

    @Data
    @NoArgsConstructor
    static class SemesterUpdateDto {
        private String name;
        private int year;
        private Season season;
    }

    @Data
    @NoArgsConstructor
    static class SemesterUpdateDatesDto {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @NoArgsConstructor
    public static class SemesterUpdateGradesDto {
        private float targetGrade;
        private float earnedGrade;
        private int completedCredits;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "List of semesters response")
    static class SemesterListResponseDto {
        private List<SemesterResponseDto> semesters;
    }
}