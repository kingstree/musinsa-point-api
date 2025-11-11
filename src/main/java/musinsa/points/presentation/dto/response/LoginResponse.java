package musinsa.points.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String authorization
) {}
