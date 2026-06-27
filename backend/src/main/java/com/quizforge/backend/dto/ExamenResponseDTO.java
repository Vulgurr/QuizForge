package com.quizforge.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ExamenResponseDTO(
        int id,
        String titulo,
        String descripcion,
        String slug,
        int creadorId,
        int categoriaId,
        LocalDateTime creadoEn,
        List<PreguntaDTO> preguntas
) {
}
