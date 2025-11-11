package musinsa.points.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import musinsa.points.domain.enums.UserRole;

// === DTOs ===
public record LoginRequest(
        @Schema(example = "1") Long memberSeq,
        @Schema(example = "MANAGER") UserRole role

) {}
