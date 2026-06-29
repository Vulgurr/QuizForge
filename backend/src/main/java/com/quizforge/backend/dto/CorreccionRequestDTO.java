package com.quizforge.backend.dto;

import java.util.List;

public record CorreccionRequestDTO(
    List<RespuestaClienteDTO> respuestas
) {
    public CorreccionRequestDTO {
        if (respuestas == null || respuestas.isEmpty()) {
            throw new IllegalArgumentException("La lista de respuestas es obligatoria");
        }
    }
}
