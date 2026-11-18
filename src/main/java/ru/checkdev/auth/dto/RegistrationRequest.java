package ru.checkdev.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(max = 100) @Schema(example = "Анна") String username,
        @NotBlank @Email @Size(max = 254) @Schema(example = "anna@example.com") String email,
        @NotBlank @Size(min = 8, max = 72)
        @Schema(example = "Study-password-2026", accessMode = Schema.AccessMode.WRITE_ONLY) String password,
        @NotNull @AssertTrue @Schema(example = "true", description = "Согласие на обработку данных") Boolean privacy) {
}
