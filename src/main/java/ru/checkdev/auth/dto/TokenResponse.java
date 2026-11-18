package ru.checkdev.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn
) {
    @Override
    public String toString() {
        return "TokenResponse[token hidden]";
    }
}
