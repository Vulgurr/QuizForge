package com.quizforge.backend.dto;

import java.util.List;

public record CategoriaRequestDTO(
        String nombre,
        String descripcion,
        List<String> apodos
) {
}
