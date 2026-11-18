package ru.checkdev.auth.dto;

import java.util.List;

public record CurrentUserResponse(int id, String username, String email, List<String> roles) {
}
