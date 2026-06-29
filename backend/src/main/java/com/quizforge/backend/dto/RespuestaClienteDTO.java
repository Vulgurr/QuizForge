package com.quizforge.backend.dto;

public record RespuestaClienteDTO(
    Integer preguntaId,
    String valorDichoPorElUsuario
) {
    public RespuestaClienteDTO {
        if (preguntaId == null || preguntaId <= 0) {
            throw new IllegalArgumentException("El ID de pregunta es obligatorio y debe ser positivo");
        }
        if (valorDichoPorElUsuario == null || valorDichoPorElUsuario.isBlank()) {
            throw new IllegalArgumentException("El valor de respuesta es obligatorio");
        }
    }
}
