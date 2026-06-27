package com.quizforge.backend.controller;

import com.quizforge.backend.dto.AuthRequestDTO;
import com.quizforge.backend.dto.AuthResponseDTO;
import com.quizforge.backend.gestor.GestorAuth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GestorAuth gestorAuth;

    public AuthController(GestorAuth gestorAuth) {
        this.gestorAuth = gestorAuth;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO dto) {
        AuthResponseDTO respuesta = gestorAuth.autenticar(dto);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/registrar")
    public ResponseEntity<AuthResponseDTO> registrar(@RequestBody AuthRequestDTO dto) {
        gestorAuth.registrarUsuario(dto);
        AuthResponseDTO respuesta = gestorAuth.autenticar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
