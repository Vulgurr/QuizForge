package com.quizforge.backend.dto;

public record AuthResponseDTO(
        String token,
        String rol
) {
}
