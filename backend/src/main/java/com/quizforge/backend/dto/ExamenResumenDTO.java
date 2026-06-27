package com.quizforge.backend.dto;

import java.time.LocalDateTime;

public record ExamenResumenDTO(
        int id,
        String titulo,
        String descripcion,
        String slug,
        LocalDateTime creadoEn
) {
}
