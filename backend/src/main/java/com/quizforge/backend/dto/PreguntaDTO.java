package com.quizforge.backend.dto;

public record PreguntaDTO(
        String tipo,
        String texto,
        String metadataJson
) {
}
