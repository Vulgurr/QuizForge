package com.quizforge.backend.dto;

public record PreguntaVerdaderoFalsoDTO(
        Integer id,
        String texto,
        boolean respuestaCorrecta,
        Integer examenId
) implements PreguntaDTO, PreguntaCreateDTO {
}
