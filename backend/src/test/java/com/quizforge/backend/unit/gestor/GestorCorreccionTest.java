package com.quizforge.backend.unit.gestor;

import com.quizforge.backend.dto.CorreccionRequestDTO;
import com.quizforge.backend.dto.CorreccionResponseDTO;
import com.quizforge.backend.dto.RespuestaClienteDTO;
import com.quizforge.backend.gestor.GestorCorreccion;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.model.pregunta.Pregunta;
import com.quizforge.backend.model.pregunta.PreguntaMultipleChoice;
import com.quizforge.backend.model.pregunta.PreguntaVerdaderoOFalso;
import com.quizforge.backend.repository.ExamenRepository;
import com.quizforge.backend.repository.PreguntaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GestorCorreccionTest {

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private PreguntaRepository preguntaRepository;

    @InjectMocks
    private GestorCorreccion gestorCorreccion;

    private Examen examen;
    private PreguntaMultipleChoice preguntaMC;
    private PreguntaVerdaderoOFalso preguntaVF;

    @BeforeEach
    void setUp() {
        examen = new Examen();
        examen.setId(1);
        examen.setTitulo("Examen de Prueba");
        examen.setCreadorId(1);
        examen.setCategoriaId(1);
        examen.setCreadoEn(LocalDateTime.now());
        examen.setPreguntas(new ArrayList<>());

        preguntaMC = new PreguntaMultipleChoice();
        preguntaMC.setId(1);
        preguntaMC.setTexto("¿Capital de Francia?");
        preguntaMC.setOpciones(List.of("Madrid", "París", "Londres", "Berlín"));
        preguntaMC.setRespuestaCorrecta("París");
        preguntaMC.setExamen(examen);

        preguntaVF = new PreguntaVerdaderoOFalso();
        preguntaVF.setId(2);
        preguntaVF.setTexto("¿La Tierra es plana?");
        preguntaVF.setRespuestaCorrecta(false);
        preguntaVF.setExamen(examen);

        examen.getPreguntas().add(preguntaMC);
        examen.getPreguntas().add(preguntaVF);
    }

    @Test
    void calcularNotaExamen_ExamenNoExistente_LanzaExcepcion() {
        when(examenRepository.findById(anyInt())).thenReturn(Optional.empty());

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(1, "París")
        ));

        assertThrows(ResponseStatusException.class, () -> {
            gestorCorreccion.calcularNotaExamen(dto, 999);
        });
    }

    @Test
    void calcularNotaExamen_ExamenSinPreguntas_LanzaExcepcion() {
        examen.setPreguntas(new ArrayList<>());
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(1, "París")
        ));

        assertThrows(ResponseStatusException.class, () -> {
            gestorCorreccion.calcularNotaExamen(dto, 1);
        });
    }

    @Test
    void calcularNotaExamen_TodasCorrectas_RetornaNotaDiez() {
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findById(1)).thenReturn(Optional.of(preguntaMC));
        when(preguntaRepository.findById(2)).thenReturn(Optional.of(preguntaVF));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(1, "París"),
            new RespuestaClienteDTO(2, "falso")
        ));

        CorreccionResponseDTO resultado = gestorCorreccion.calcularNotaExamen(dto, 1);

        assertEquals(1, resultado.examenId());
        assertEquals(10.0, resultado.puntajeFinal());
        assertEquals(2, resultado.totalPreguntas());
        assertEquals(2, resultado.correctas());
    }

    @Test
    void calcularNotaExamen_TodasIncorrectas_RetornaNotaCero() {
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findById(1)).thenReturn(Optional.of(preguntaMC));
        when(preguntaRepository.findById(2)).thenReturn(Optional.of(preguntaVF));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(1, "Madrid"),
            new RespuestaClienteDTO(2, "verdadero")
        ));

        CorreccionResponseDTO resultado = gestorCorreccion.calcularNotaExamen(dto, 1);

        assertEquals(1, resultado.examenId());
        assertEquals(0.0, resultado.puntajeFinal());
        assertEquals(2, resultado.totalPreguntas());
        assertEquals(0, resultado.correctas());
    }

    @Test
    void calcularNotaExamen_Mixtas_RetornaNotaParcial() {
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findById(1)).thenReturn(Optional.of(preguntaMC));
        when(preguntaRepository.findById(2)).thenReturn(Optional.of(preguntaVF));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(1, "París"),
            new RespuestaClienteDTO(2, "verdadero")
        ));

        CorreccionResponseDTO resultado = gestorCorreccion.calcularNotaExamen(dto, 1);

        assertEquals(1, resultado.examenId());
        assertEquals(5.0, resultado.puntajeFinal());
        assertEquals(2, resultado.totalPreguntas());
        assertEquals(1, resultado.correctas());
    }

    @Test
    void calcularNotaExamen_IgnoraRespuestasDeOtroExamen() {
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findById(1)).thenReturn(Optional.of(preguntaMC));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(1, "París"),
            new RespuestaClienteDTO(3, "Madrid")
        ));

        CorreccionResponseDTO resultado = gestorCorreccion.calcularNotaExamen(dto, 1);

        assertEquals(1, resultado.examenId());
        assertEquals(10.0, resultado.puntajeFinal());
        assertEquals(2, resultado.totalPreguntas());
        assertEquals(1, resultado.correctas());
    }

    @Test
    void calcularNotaExamen_CaseInsensitive_MultipleChoice() {
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findById(1)).thenReturn(Optional.of(preguntaMC));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(1, "PARÍS")
        ));

        CorreccionResponseDTO resultado = gestorCorreccion.calcularNotaExamen(dto, 1);

        assertEquals(10.0, resultado.puntajeFinal());
        assertEquals(1, resultado.correctas());
    }

    @Test
    void calcularNotaExamen_ParseoBooleano_Verdadero() {
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findById(2)).thenReturn(Optional.of(preguntaVF));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(2, "verdadero")
        ));

        CorreccionResponseDTO resultado = gestorCorreccion.calcularNotaExamen(dto, 1);

        assertEquals(0.0, resultado.puntajeFinal());
        assertEquals(0, resultado.correctas());
    }

    @Test
    void calcularNotaExamen_ParseoBooleano_Si() {
        when(examenRepository.findById(1)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findById(2)).thenReturn(Optional.of(preguntaVF));

        CorreccionRequestDTO dto = new CorreccionRequestDTO(List.of(
            new RespuestaClienteDTO(2, "si")
        ));

        CorreccionResponseDTO resultado = gestorCorreccion.calcularNotaExamen(dto, 1);

        assertEquals(0.0, resultado.puntajeFinal());
        assertEquals(0, resultado.correctas());
    }
}
