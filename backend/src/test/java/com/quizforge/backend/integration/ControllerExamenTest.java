package com.quizforge.backend.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizforge.backend.controller.ExamenController;
import com.quizforge.backend.dto.CorreccionRequestDTO;
import com.quizforge.backend.dto.CorreccionResponseDTO;
import com.quizforge.backend.dto.ExamenRequestDTO;
import com.quizforge.backend.dto.PreguntaMultipleChoiceDTO;
import com.quizforge.backend.dto.RespuestaClienteDTO;
import com.quizforge.backend.gestor.GestorCorreccion;
import com.quizforge.backend.gestor.GestorExamen;
import com.quizforge.backend.gestor.GestorSeguridad;
import com.quizforge.backend.security.RateLimitInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExamenController.class)
class ExamenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // A partir de Spring Boot 3.4+, usamos @MockitoBean
    @MockitoBean
    private GestorExamen gestorExamen;

    @MockitoBean
    private GestorSeguridad gestorSeguridad;

    @MockitoBean
    private GestorCorreccion gestorCorreccion;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"}) // Simula un usuario logueado
    void crearExamen_jsonPolimorficoValido_retornaStatusCreated() throws Exception {
        // Arrange
        PreguntaMultipleChoiceDTO pregunta = new PreguntaMultipleChoiceDTO(
                null,
                "¿Qué patrón usa Spring para los objetos?",
                List.of("Singleton", "Prototype"),
                "Singleton",
                null
        );

        ExamenRequestDTO requestDTO = new ExamenRequestDTO(
                "Arquitectura Spring",
                "Examen avanzado",
                10,
                List.of(pregunta)
        );

        // Simulamos que el interceptor deja pasar la petición
        try {
            when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        } catch (Exception e) {
            // Ignorado para el setup del mock
        }

        // Act & Assert
        mockMvc.perform(post("/api/examenes")
                        .with(csrf())
                        .header("Authorization", "Bearer token_falso_valido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        // Verificamos que se llamó a la lógica de negocio
        verify(gestorExamen).crearExamen(any(ExamenRequestDTO.class), anyInt());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void corregirExamen_respuestasValidas_retornaStatusOk() throws Exception {
        // Arrange
        CorreccionResponseDTO responseDTO = new CorreccionResponseDTO(
                1,
                8.5,
                10,
                8
        );

        CorreccionRequestDTO requestDTO = new CorreccionRequestDTO(
                List.of(
                        new RespuestaClienteDTO(1, "París"),
                        new RespuestaClienteDTO(2, "falso")
                )
        );

        when(gestorCorreccion.calcularNotaExamen(any(CorreccionRequestDTO.class), anyInt()))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/examenes/1/corregir")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.examenId").value(1))
                .andExpect(jsonPath("$.puntajeFinal").value(8.5))
                .andExpect(jsonPath("$.totalPreguntas").value(10))
                .andExpect(jsonPath("$.correctas").value(8));

        verify(gestorCorreccion).calcularNotaExamen(any(CorreccionRequestDTO.class), eq(1));
    }
}