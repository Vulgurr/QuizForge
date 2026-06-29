package com.quizforge.backend.dto;

import java.util.List;

public sealed interface PreguntaUpdateDTO permits
        PreguntaUpdateDTO.PreguntaUpdateMultipleChoiceDTO,
        PreguntaUpdateDTO.PreguntaUpdateVerdaderoFalsoDTO,
        PreguntaUpdateDTO.PreguntaUpdateDesarrolloDeterministicoDTO,
        PreguntaUpdateDTO.PreguntaUpdateDesarrolloNoDeterministicoDTO {

    String texto();

    record PreguntaUpdateMultipleChoiceDTO(
        String texto,
        List<String> opciones,
        String respuestaCorrecta
    ) implements PreguntaUpdateDTO {}

    record PreguntaUpdateVerdaderoFalsoDTO(
        String texto,
        boolean respuestaCorrecta
    ) implements PreguntaUpdateDTO {}

    record PreguntaUpdateDesarrolloDeterministicoDTO(
        String texto,
        String respuestaEsperadaExacta
    ) implements PreguntaUpdateDTO {}

    record PreguntaUpdateDesarrolloNoDeterministicoDTO(
        String texto,
        String rubricaEvaluacion
    ) implements PreguntaUpdateDTO {}
}
