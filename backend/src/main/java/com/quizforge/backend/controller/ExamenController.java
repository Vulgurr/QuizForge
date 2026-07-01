package com.quizforge.backend.controller;

import com.quizforge.backend.dto.CorreccionRequestDTO;
import com.quizforge.backend.dto.CorreccionResponseDTO;
import com.quizforge.backend.dto.ExamenRequestDTO;
import com.quizforge.backend.dto.ExamenResumenDTO;
import com.quizforge.backend.dto.ExamenResponseDTO;
import com.quizforge.backend.gestor.GestorCorreccion;
import com.quizforge.backend.gestor.GestorExamen;
import com.quizforge.backend.gestor.GestorSeguridad;
import com.quizforge.backend.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examenes")
public class ExamenController {

    private final GestorExamen gestorExamen;
    private final GestorSeguridad gestorSeguridad;
    private final GestorCorreccion gestorCorreccion;

    public ExamenController(GestorExamen gestorExamen, GestorSeguridad gestorSeguridad, GestorCorreccion gestorCorreccion) {
        this.gestorExamen = gestorExamen;
        this.gestorSeguridad = gestorSeguridad;
        this.gestorCorreccion = gestorCorreccion;
    }
    @GetMapping("/{slug}")
    public ResponseEntity<ExamenResponseDTO> obtenerExamenPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(gestorExamen.obtenerExamenPorSlug(slug));
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

    @PostMapping("/{examenId}/corregir")
    public ResponseEntity<CorreccionResponseDTO> corregirExamen(
            @PathVariable int examenId,
            @RequestBody CorreccionRequestDTO dto
    ) {
        CorreccionResponseDTO respuesta = gestorCorreccion.calcularNotaExamen(dto, examenId);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/mis-examenes")
    public ResponseEntity<List<ExamenResponseDTO>> listarMisExamenes(
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        return ResponseEntity.ok(gestorExamen.obtenerMisExamenes(usuarioId));
    }

    @PutMapping("/{examenId}")
    public ResponseEntity<ExamenResponseDTO> actualizarExamen(
            @PathVariable int examenId,
            @RequestBody ExamenRequestDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        String rol = gestorSeguridad.extraerRol(token);

        ExamenResponseDTO respuesta = gestorExamen.actualizarExamen(examenId, dto, usuarioId, rol);
        return ResponseEntity.ok(respuesta);
    }
}
