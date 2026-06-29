package com.quizforge.backend.dto;

public record CorreccionResponseDTO(
    Integer examenId,
    Double puntajeFinal,
    Integer totalPreguntas,
    Integer correctas
) {
    public CorreccionResponseDTO {
        if (examenId == null || examenId <= 0) {
            throw new IllegalArgumentException("El ID de examen es obligatorio y debe ser positivo");
        }
        if (puntajeFinal == null || puntajeFinal < 0 || puntajeFinal > 10) {
            throw new IllegalArgumentException("El puntaje final debe estar entre 0 y 10");
        }
        if (totalPreguntas == null || totalPreguntas < 0) {
            throw new IllegalArgumentException("El total de preguntas no puede ser negativo");
        }
        if (correctas == null || correctas < 0) {
            throw new IllegalArgumentException("El número de correctas no puede ser negativo");
        }
    }
}
