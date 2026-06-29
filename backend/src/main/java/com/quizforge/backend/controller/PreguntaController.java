package com.quizforge.backend.controller;

import com.quizforge.backend.dto.PreguntaCreateDTO;
import com.quizforge.backend.dto.PreguntaDTO;
import com.quizforge.backend.dto.PreguntaUpdateDTO;
import com.quizforge.backend.gestor.GestorPregunta;
import com.quizforge.backend.gestor.GestorSeguridad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    private final GestorPregunta gestorPregunta;
    private final GestorSeguridad gestorSeguridad;

    public PreguntaController(GestorPregunta gestorPregunta, GestorSeguridad gestorSeguridad) {
        this.gestorPregunta = gestorPregunta;
        this.gestorSeguridad = gestorSeguridad;
    }

    @PostMapping
    public ResponseEntity<PreguntaDTO> crearPregunta(
            @RequestBody PreguntaCreateDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        String rol = gestorSeguridad.extraerRol(token);
        PreguntaDTO respuesta = gestorPregunta.crearPregunta(dto, usuarioId, rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/masa")
    public ResponseEntity<Void> crearPreguntasEnMasa(
            @RequestBody List<PreguntaCreateDTO> dtos,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        String rol = gestorSeguridad.extraerRol(token);
        gestorPregunta.crearPreguntasEnMasa(dtos, usuarioId, rol);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{preguntaId}")
    public ResponseEntity<Void> eliminarPregunta(
            @PathVariable int preguntaId,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        String rol = gestorSeguridad.extraerRol(token);
        gestorPregunta.eliminarPregunta(preguntaId, usuarioId, rol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/examen/{examenId}")
    public ResponseEntity<List<PreguntaDTO>> listarPreguntasPorExamen(
            @PathVariable int examenId
    ) {
        return ResponseEntity.ok(gestorPregunta.listarPreguntasPorExamen(examenId));
    }

    @PutMapping("/{preguntaId}")
    public ResponseEntity<PreguntaDTO> modificarPregunta(
            @PathVariable int preguntaId,
            @RequestBody PreguntaUpdateDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        int usuarioId = gestorSeguridad.extraerUsuarioId(token);
        String rol = gestorSeguridad.extraerRol(token);
        PreguntaDTO respuesta = gestorPregunta.modificarPregunta(preguntaId, dto, usuarioId, rol);
        return ResponseEntity.ok(respuesta);
    }
}
