package com.quizforge.backend.dto;

public record PreguntaDesarrolloNoDeterministicoDTO(
        Integer id,
        String texto,
        String rubricaEvaluacion,
        Integer examenId
) implements PreguntaDTO, PreguntaCreateDTO {
}
