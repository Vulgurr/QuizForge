package com.quizforge.backend.unit.gestor;


import com.quizforge.backend.dto.*;
import com.quizforge.backend.gestor.GestorPregunta;
import com.quizforge.backend.gestor.GestorSeguridad;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.model.pregunta.Pregunta;
import com.quizforge.backend.model.pregunta.PreguntaMultipleChoice;
import com.quizforge.backend.model.pregunta.PreguntaVerdaderoOFalso;
import com.quizforge.backend.repository.ExamenRepository;
import com.quizforge.backend.repository.PreguntaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorPreguntaTest {

    @Mock
    private PreguntaRepository preguntaRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private GestorSeguridad gestorSeguridad;

    @InjectMocks
    private GestorPregunta gestorPregunta;

    @Test
    void crearPregunta_datosValidosVerdaderoFalso_retornaPreguntaDTO() {
        // Arrange
        int usuarioId = 1;
        int examenId = 50;
        String rol = "USER";

        PreguntaCreateDTO dto = new PreguntaVerdaderoFalsoDTO(
                null,
                "¿Spring Boot es un framework de Java?",
                true,
                examenId
        );

        Examen examen = new Examen();
        examen.setId(examenId);
        examen.setCreadorId(usuarioId);

        when(examenRepository.findById(examenId)).thenReturn(Optional.of(examen));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol)).thenReturn(true);

        when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(invocation -> {
            PreguntaVerdaderoOFalso p = invocation.getArgument(0);
            p.setId(10);
            p.setExamen(examen);
            return p;
        });

        // Act
        PreguntaDTO resultado = gestorPregunta.crearPregunta(dto, usuarioId, rol);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isInstanceOf(PreguntaVerdaderoFalsoDTO.class);
        assertThat(resultado.texto()).isEqualTo("¿Spring Boot es un framework de Java?");
        assertThat(((PreguntaVerdaderoFalsoDTO) resultado).respuestaCorrecta()).isTrue();

        verify(examenRepository, times(1)).findById(examenId);
        verify(preguntaRepository, times(1)).save(any(Pregunta.class));
    }

    @Test
    void crearPregunta_usuarioNoAutorizado_lanzaExcepcion() {
        // Arrange
        int usuarioIdIntruso = 99;
        int examenId = 50;
        String rol = "USER";

        PreguntaCreateDTO dto = new PreguntaMultipleChoiceDTO(
                null,
                "Pregunta intrusa",
                List.of("A", "B"),
                "A",
                examenId
        );

        Examen examen = new Examen();
        examen.setId(examenId);
        examen.setCreadorId(1); // El creador real es 1

        when(examenRepository.findById(examenId)).thenReturn(Optional.of(examen));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioIdIntruso, examen.getCreadorId(), rol)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> gestorPregunta.crearPregunta(dto, usuarioIdIntruso, rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No tenés permiso para agregar preguntas");

        verify(preguntaRepository, never()).save(any(Pregunta.class));
    }

    @Test
    void crearPreguntasEnMasa_listaVacia_lanzaExcepcion() {
        // Arrange
        int usuarioId = 1;
        String rol = "USER";
        List<PreguntaCreateDTO> listaVacia = List.of();

        // Act & Assert
        assertThatThrownBy(() -> gestorPregunta.crearPreguntasEnMasa(listaVacia, usuarioId, rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("La lista de preguntas no puede estar vacía");

        verify(preguntaRepository, never()).save(any());
    }

    @Test
    void eliminarPregunta_usuarioAutorizado_eliminaExitosamente() {
        // Arrange
        int preguntaId = 10;
        int usuarioId = 1;
        String rol = "ADMIN";

        Examen examen = new Examen();
        examen.setCreadorId(usuarioId);

        PreguntaVerdaderoOFalso preguntaExistente = new PreguntaVerdaderoOFalso();
        preguntaExistente.setId(preguntaId);
        preguntaExistente.setExamen(examen); // Acá anidamos el examen en la pregunta

        when(preguntaRepository.findById(preguntaId)).thenReturn(Optional.of(preguntaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol)).thenReturn(true);

        // Act
        gestorPregunta.eliminarPregunta(preguntaId, usuarioId, rol);

        // Assert
        verify(preguntaRepository, times(1)).findById(preguntaId);
        verify(gestorSeguridad, times(1)).esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol);
        verify(preguntaRepository, times(1)).deleteById(preguntaId);
    }

    @Test
    void listarPreguntasPorExamen_examenConMultiplesTipos_retornaListaPolimorfica() {
        // Arrange
        int examenId = 50;
        Examen examen = new Examen();
        examen.setId(examenId);

        PreguntaVerdaderoOFalso p1 = new PreguntaVerdaderoOFalso();
        p1.setId(1);
        p1.setTexto("Pregunta 1");
        p1.setRespuestaCorrecta(true);
        p1.setExamen(examen);

        PreguntaMultipleChoice p2 = new PreguntaMultipleChoice();
        p2.setId(2);
        p2.setTexto("Pregunta 2");
        p2.setOpciones(List.of("A", "B"));
        p2.setRespuestaCorrecta("A");
        p2.setExamen(examen);

        when(preguntaRepository.findByExamenId(examenId)).thenReturn(List.of(p1, p2));

        // Act
        List<PreguntaDTO> resultados = gestorPregunta.listarPreguntasPorExamen(examenId);

        // Assert
        assertThat(resultados).hasSize(2);
        assertThat(resultados.get(0)).isInstanceOf(PreguntaVerdaderoFalsoDTO.class);
        assertThat(resultados.get(1)).isInstanceOf(PreguntaMultipleChoiceDTO.class);

        verify(preguntaRepository, times(1)).findByExamenId(examenId);
    }

    @Test
    void modificarPregunta_usuarioEsPropietario_modificaExitosamente() {
        // Arrange
        int preguntaId = 10;
        int usuarioId = 1;
        String rol = "USER";

        Examen examen = new Examen();
        examen.setId(50);
        examen.setCreadorId(usuarioId);

        PreguntaMultipleChoice preguntaExistente = new PreguntaMultipleChoice();
        preguntaExistente.setId(preguntaId);
        preguntaExistente.setTexto("Pregunta original");
        preguntaExistente.setOpciones(List.of("A", "B"));
        preguntaExistente.setRespuestaCorrecta("A");
        preguntaExistente.setExamen(examen);

        PreguntaUpdateDTO updateDTO = new PreguntaUpdateDTO.PreguntaUpdateMultipleChoiceDTO(
                "Pregunta modificada",
                List.of("A", "B", "C"),
                "B"
        );

        when(preguntaRepository.findById(preguntaId)).thenReturn(Optional.of(preguntaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol)).thenReturn(true);
        when(preguntaRepository.save(any(PreguntaMultipleChoice.class))).thenReturn(preguntaExistente);

        // Act
        PreguntaDTO resultado = gestorPregunta.modificarPregunta(preguntaId, updateDTO, usuarioId, rol);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.texto()).isEqualTo("Pregunta modificada");

        verify(preguntaRepository, times(1)).findById(preguntaId);
        verify(gestorSeguridad, times(1)).esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol);
        verify(preguntaRepository, times(1)).save(any(PreguntaMultipleChoice.class));
    }

    @Test
    void modificarPregunta_usuarioNoAutorizado_lanzaExcepcion() {
        // Arrange
        int preguntaId = 10;
        int usuarioIdIntruso = 99;
        String rol = "USER";

        Examen examen = new Examen();
        examen.setId(50);
        examen.setCreadorId(1);

        PreguntaMultipleChoice preguntaExistente = new PreguntaMultipleChoice();
        preguntaExistente.setId(preguntaId);
        preguntaExistente.setExamen(examen);

        PreguntaUpdateDTO updateDTO = new PreguntaUpdateDTO.PreguntaUpdateMultipleChoiceDTO(
                "Pregunta modificada",
                List.of("A", "B"),
                "A"
        );

        when(preguntaRepository.findById(preguntaId)).thenReturn(Optional.of(preguntaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioIdIntruso, examen.getCreadorId(), rol)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> gestorPregunta.modificarPregunta(preguntaId, updateDTO, usuarioIdIntruso, rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No tenés permiso para modificar esta pregunta");

        verify(preguntaRepository, never()).save(any(Pregunta.class));
    }

    @Test
    void modificarPregunta_tipoIncompatible_lanzaExcepcion() {
        // Arrange
        int preguntaId = 10;
        int usuarioId = 1;
        String rol = "USER";

        Examen examen = new Examen();
        examen.setId(50);
        examen.setCreadorId(usuarioId);

        PreguntaVerdaderoOFalso preguntaExistente = new PreguntaVerdaderoOFalso();
        preguntaExistente.setId(preguntaId);
        preguntaExistente.setExamen(examen);

        PreguntaUpdateDTO updateDTO = new PreguntaUpdateDTO.PreguntaUpdateMultipleChoiceDTO(
                "Pregunta modificada",
                List.of("A", "B"),
                "A"
        );

        when(preguntaRepository.findById(preguntaId)).thenReturn(Optional.of(preguntaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> gestorPregunta.modificarPregunta(preguntaId, updateDTO, usuarioId, rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tipo de pregunta incompatible");

        verify(preguntaRepository, never()).save(any(Pregunta.class));
    }
}