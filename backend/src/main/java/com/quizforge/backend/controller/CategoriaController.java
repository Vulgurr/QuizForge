package com.quizforge.backend.controller;

import com.quizforge.backend.dto.CategoriaRequestDTO;
import com.quizforge.backend.dto.CategoriaResponseDTO;
import com.quizforge.backend.dto.ExamenResumenDTO;
import com.quizforge.backend.gestor.GestorCategoria;
import com.quizforge.backend.gestor.GestorSeguridad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final GestorCategoria gestorCategoria;
    private final GestorSeguridad gestorSeguridad;

    public CategoriaController(GestorCategoria gestorCategoria, GestorSeguridad gestorSeguridad) {
        this.gestorCategoria = gestorCategoria;
        this.gestorSeguridad = gestorSeguridad;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crearCategoria(
            @RequestBody CategoriaRequestDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        CategoriaResponseDTO respuesta = gestorCategoria.crearCategoria(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @DeleteMapping("/{categoriaId}")
    public ResponseEntity<Void> eliminarCategoria(
            @PathVariable int categoriaId,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        String rol = gestorSeguridad.extraerRol(token);

        gestorCategoria.eliminarCategoria(categoriaId, usuarioId, rol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(gestorCategoria.listarCategorias());
    }

    @GetMapping("/{categoriaSlug}/examenes")
    public ResponseEntity<List<ExamenResumenDTO>> obtenerExamenesPorCategoria(
            @PathVariable String categoriaSlug
    ) {
        return ResponseEntity.ok(gestorCategoria.obtenerExamenesPorCategoria(categoriaSlug));
    }

    @GetMapping("/mis-categorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listarMisCategorias(
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        return ResponseEntity.ok(gestorCategoria.listarCategoriasPorCreador(usuarioId));
    }
}