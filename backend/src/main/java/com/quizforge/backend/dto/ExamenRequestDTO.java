package com.quizforge.backend.dto;

import java.util.List;

public record ExamenRequestDTO(
        String titulo,
        String descripcion,
        int categoriaId,
        List<PreguntaDTO> preguntas
) {
}
