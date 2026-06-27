package com.quizforge.backend.dto;

public record AuthRequestDTO(
        String email,
        String password
) {
}
