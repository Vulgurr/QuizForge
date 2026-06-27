package com.quizforge.backend.dto;

public record AuthResponseDTO(
        String tokenJwt,
        String rol
) {
}
