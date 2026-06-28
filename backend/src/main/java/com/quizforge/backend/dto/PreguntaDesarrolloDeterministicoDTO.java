package com.quizforge.backend.dto;

public record PreguntaDesarrolloDeterministicoDTO(
        Integer id,
        String texto,
        String respuestaEsperadaExacta,
        Integer examenId
) implements PreguntaDTO, PreguntaCreateDTO {
}
