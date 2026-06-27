package com.quizforge.backend.dto;

import java.util.List;

public record CategoriaResponseDTO(
        int id,
        String nombre,
        String descripcion,
        String slug,
        List<String> apodos
) {
}
