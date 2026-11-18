package ru.checkdev.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email @Size(max = 254) @Schema(example = "anna@example.com") String email,
        @NotBlank @Size(max = 72) @Schema(example = "Study-password-2026", format = "password") String password
) {
    @Override
    public String toString() {
        return "LoginRequest[credentials hidden]";
    }
}
