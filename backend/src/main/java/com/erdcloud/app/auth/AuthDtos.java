package com.erdcloud.app.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SignupRequest(
        @NotBlank @Size(min = 4, max = 50) String loginId,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(min = 2, max = 80) String displayName
    ) {
    }

    public record LoginRequest(
        @NotBlank String loginId,
        @NotBlank String password
    ) {
    }

    public record AuthResponse(
        String accessToken,
        Long userId,
        String loginId,
        String displayName
    ) {
    }

    public record MeResponse(
        Long id,
        String loginId,
        String email,
        String displayName
    ) {
    }
}

