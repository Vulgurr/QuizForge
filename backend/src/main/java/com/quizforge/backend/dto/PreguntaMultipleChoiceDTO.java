package com.quizforge.backend.dto;

import java.util.List;

public record PreguntaMultipleChoiceDTO(
        Integer id,
        String texto,
        List<String> opciones,
        String respuestaCorrecta,
        Integer examenId
) implements PreguntaDTO, PreguntaCreateDTO {
}
