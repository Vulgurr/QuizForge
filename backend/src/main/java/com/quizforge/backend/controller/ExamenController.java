package com.quizforge.backend.controller;

import com.quizforge.backend.dto.ExamenRequestDTO;
import com.quizforge.backend.dto.ExamenResponseDTO;
import com.quizforge.backend.gestor.GestorExamen;
import com.quizforge.backend.gestor.GestorSeguridad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/examenes")
public class ExamenController {

    private final GestorExamen gestorExamen;
    private final GestorSeguridad gestorSeguridad;

    public ExamenController(GestorExamen gestorExamen, GestorSeguridad gestorSeguridad) {
        this.gestorExamen = gestorExamen;
        this.gestorSeguridad = gestorSeguridad;
    }

    @PostMapping
    public ResponseEntity<ExamenResponseDTO> crearExamen(
            @RequestBody ExamenRequestDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        ExamenResponseDTO respuesta = gestorExamen.crearExamen(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @DeleteMapping("/{examenId}")
    public ResponseEntity<Void> eliminarExamen(
            @PathVariable int examenId,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        String rol = gestorSeguridad.extraerRol(token);
        gestorExamen.eliminarExamen(examenId, usuarioId, rol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoriaSlug}/{examenSlug}")
    public ResponseEntity<ExamenResponseDTO> obtenerExamen(
            @PathVariable String categoriaSlug,
            @PathVariable String examenSlug
    ) {
        ExamenResponseDTO respuesta = gestorExamen.obtenerPorSlugs(categoriaSlug, examenSlug);
        return ResponseEntity.ok(respuesta);
    }
}
