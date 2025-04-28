package com.example.api.controller;

import com.example.api.controller.dto.semester.*;
import com.example.api.entity.enums.Season;
import com.example.api.service.SemesterService;
import com.example.api.service.dto.semester.*;
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

import java.util.Optional;
import java.util.UUID;

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
                            content = @Content(schema = @Schema(implementation = SemesterListResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<SemesterListResponse> getSemesters() {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        SemesterListOutput semesterListOutput = semesterService.findSemestersByUserId(userId);
        if (semesterListOutput.getSemesters().isEmpty()) {
            return ResponseEntity.ok(new SemesterListResponse());
        }

        // Ownership check (optional if service layer enforces this)
        if (!semesterListOutput.getSemesters().get(0).getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SemesterListResponse response = new SemesterListResponse(
                semesterListOutput.getSemesters().stream()
                        .map(SemesterResponse::fromServiceDto)
                        .toList()
        );
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
                            content = @Content(schema = @Schema(implementation = SemesterResponse.class))
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
    public ResponseEntity<SemesterResponse> getSemesterByYearAndSeason(
            @RequestParam int year,
            @RequestParam String season) {
        // TODO(mj): Obtain authenticated user ID instead of placeholder
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate year
            if (year < 0) {
                return ResponseEntity.badRequest().build();
            }
            // Check if the season is valid
            if (season == null) {
                return ResponseEntity.badRequest().build();
            }
            Season seasonVal = Season.fromString(season);

            // Check semester exists and user owns it.
            Optional<SemesterOutput> semesterOutput = semesterService.findSemesterByUserAndYearAndSeason(userId, year, seasonVal);
            if (semesterOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!semesterOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            SemesterResponse responseDto = SemesterResponse.fromServiceDto(semesterOutput.get());
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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
                            content = @Content(schema = @Schema(implementation = SemesterResponse.class))
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
    public ResponseEntity<SemesterResponse> getSemesterById(@PathVariable UUID id) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check semester exists and user owns it.
            Optional<SemesterOutput> semesterOutput = semesterService.findSemesterById(id);
            if (semesterOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!semesterOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            SemesterResponse responseDto = SemesterResponse.fromServiceDto(semesterOutput.get());
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
                    content = @Content(schema = @Schema(implementation = CreateSemesterRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Semester created successfully",
                            content = @Content(schema = @Schema(implementation = SemesterResponse.class))
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
    public ResponseEntity<SemesterResponse> createSemester(
            @RequestBody CreateSemesterRequest request) {
        // TODO(mj): Obtain authenticated user ID instead of placeholder
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate year
            if (request.getYear() < 0) {
                return ResponseEntity.badRequest().build();
            }
            // Check if the season is valid
            if (request.getSeason() == null) {
                return ResponseEntity.badRequest().build();
            }
            Season season = Season.fromString(request.getSeason());

            CreateSemesterInput input = new CreateSemesterInput();
            input.setUserId(userId);
            input.setName(request.getName());
            input.setYear(request.getYear());
            input.setSeason(season);

            SemesterOutput createdSemesterOutput = semesterService.createSemester(input);
            SemesterResponse responseDto = SemesterResponse.fromServiceDto(createdSemesterOutput);
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
                    content = @Content(schema = @Schema(implementation = UpdateSemesterRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Semester updated successfully",
                            content = @Content(schema = @Schema(implementation = SemesterResponse.class))
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
    public ResponseEntity<SemesterResponse> updateSemester(
            @PathVariable UUID id,
            @RequestBody UpdateSemesterRequest request) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Validate year
            if (request.getYear() < 0) {
                return ResponseEntity.badRequest().build();
            }
            // Check if the season is valid
            if (request.getSeason() == null) {
                return ResponseEntity.badRequest().build();
            }
            Season season = Season.fromString(request.getSeason());

            // Check semester exists and user owns it.
            Optional<SemesterOutput> existingSemesterOutput = semesterService.findSemesterById(id);
            if (existingSemesterOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemesterOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            UpdateSemesterInput input = new UpdateSemesterInput();
            input.setId(id);
            input.setName(request.getName());
            input.setYear(request.getYear());
            input.setSeason(season);

            SemesterOutput updatedSemesterOutput = semesterService.updateSemester(input);
            SemesterResponse responseDto = SemesterResponse.fromServiceDto(updatedSemesterOutput);
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
                    content = @Content(schema = @Schema(implementation = UpdateSemesterDatesRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Semester dates updated successfully",
                            content = @Content(schema = @Schema(implementation = SemesterResponse.class))
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
    public ResponseEntity<SemesterResponse> updateSemesterDates(
            @PathVariable UUID id,
            @RequestBody UpdateSemesterDatesRequest request) {
        // TODO(mj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            // Check if start and end dates are provided
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().build();
            }
            // Check if start date is before end date
            if (request.getEndDate().isBefore(request.getStartDate())) {
                return ResponseEntity.badRequest().build();
            }

            // Check semester exists and user owns it.
            Optional<SemesterOutput> existingSemesterOutput = semesterService.findSemesterById(id);
            if (existingSemesterOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemesterOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            UpdateSemesterDatesInput input = new UpdateSemesterDatesInput();
            input.setId(id);
            input.setStartDate(request.getStartDate());
            input.setEndDate(request.getEndDate());

            SemesterOutput updatedSemesterOutput = semesterService.updateSemesterDates(input);
            SemesterResponse responseDto = SemesterResponse.fromServiceDto(updatedSemesterOutput);
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
                    content = @Content(schema = @Schema(implementation = UpdateSemesterGradesRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Semester grades updated successfully",
                            content = @Content(schema = @Schema(implementation = SemesterResponse.class))
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
    public ResponseEntity<SemesterResponse> updateSemesterGrades(
            @PathVariable UUID id,
            @RequestBody UpdateSemesterGradesRequest request) {
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

            // Check semester exists and user owns it.
            Optional<SemesterOutput> existingSemesterOutput = semesterService.findSemesterById(id);
            if (existingSemesterOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemesterOutput.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            UpdateSemesterGradesInput input = new UpdateSemesterGradesInput();
            input.setId(id);
            input.setTargetGrade(request.getTargetGrade());
            input.setEarnedGrade(request.getEarnedGrade());
            input.setCompletedCredits(request.getCompletedCredits());

            SemesterOutput updatedSemesterOutput = semesterService.updateSemesterGrades(input);
            SemesterResponse responseDto = SemesterResponse.fromServiceDto(updatedSemesterOutput);
            return ResponseEntity.ok(responseDto);
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
            Optional<SemesterOutput> existingSemesterOutput = semesterService.findSemesterById(id);
            if (existingSemesterOutput.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!existingSemesterOutput.get().getUserId().equals(userId)) {
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
}
