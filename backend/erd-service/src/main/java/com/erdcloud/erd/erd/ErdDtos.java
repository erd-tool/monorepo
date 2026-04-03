package com.erdcloud.erd.erd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ErdDtos {

    private ErdDtos() {
    }

    public record CreateErdRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 255) String description,
        @Pattern(regexp = "public|private") String visibility,
        Long teamId
    ) {
    }

    public record UpdateErdRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 255) String description,
        @Pattern(regexp = "public|private") String visibility,
        @NotBlank String contentJson
    ) {
    }

    public record ErdSummary(
        Long id,
        String title,
        String description,
        String visibility,
        Long teamId,
        String updatedAt
    ) {
    }

    public record ErdDetail(
        Long id,
        String title,
        String description,
        String visibility,
        Long teamId,
        String contentJson
    ) {
    }
}
