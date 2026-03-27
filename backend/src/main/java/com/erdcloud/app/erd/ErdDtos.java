package com.erdcloud.app.erd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ErdDtos {

    private ErdDtos() {
    }

    public record CreateErdRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 255) String description,
        Long teamId
    ) {
    }

    public record UpdateErdRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 255) String description,
        @NotBlank String contentJson
    ) {
    }

    public record ErdSummary(
        Long id,
        String title,
        String description,
        Long teamId,
        String teamName,
        String updatedAt
    ) {
    }

    public record ErdDetail(
        Long id,
        String title,
        String description,
        Long teamId,
        String teamName,
        String contentJson
    ) {
    }
}

