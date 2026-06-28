package com.quizforge.backend.unit.gestor;
import com.quizforge.backend.dto.*;
import com.quizforge.backend.gestor.GestorExamen;
import com.quizforge.backend.gestor.GestorSeguridad;
import com.quizforge.backend.model.Categoria;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.model.pregunta.PreguntaMultipleChoice;
import com.quizforge.backend.repository.CategoriaRepository;
import com.quizforge.backend.repository.ExamenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorExamenTest {

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private GestorSeguridad gestorSeguridad;

    @InjectMocks
    private GestorExamen gestorExamen;

    @Test
    void crearExamen_datosValidosConPreguntaMultipleChoice_retornaExamenResponseDTO() {
        // Arrange
        int usuarioId = 1;
        int categoriaId = 10;

        PreguntaDTO preguntaDTO = new PreguntaMultipleChoiceDTO(
                null,
                "¿Qué es Java?",
                List.of("Un lenguaje", "Un café", "Un SO"),
                "Un lenguaje",
                null
        );

        ExamenRequestDTO requestDTO = new ExamenRequestDTO(
                "Programación Java",
                "Examen básico",
                categoriaId,
                List.of(preguntaDTO)
        );

        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));
        when(examenRepository.findBySlugAndCategoriaId("programacion-java", categoriaId)).thenReturn(Optional.empty());

        when(examenRepository.save(any(Examen.class))).thenAnswer(invocation -> {
            Examen ex = invocation.getArgument(0);
            ex.setId(100); // Simulamos el ID autogenerado
            return ex;
        });

        // Act
        ExamenResponseDTO resultado = gestorExamen.crearExamen(requestDTO, usuarioId);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("Programación Java");
        assertThat(resultado.slug()).isEqualTo("programacion-java");
        assertThat(resultado.preguntas()).hasSize(1);
        assertThat(resultado.preguntas().get(0)).isInstanceOf(PreguntaMultipleChoiceDTO.class);

        verify(categoriaRepository, times(1)).findById(categoriaId);
        verify(examenRepository, times(1)).save(any(Examen.class));
    }

    @Test
    void crearExamen_preguntaMultipleChoiceSinOpciones_lanzaExcepcion() {
        // Arrange
        int usuarioId = 1;
        int categoriaId = 10;

        // Creamos una pregunta sin opciones (lista vacía)
        PreguntaDTO preguntaInvalida = new PreguntaMultipleChoiceDTO(
                null,
                "¿Qué es Java?",
                List.of(),
                "Un lenguaje",
                null
        );

        ExamenRequestDTO requestDTO = new ExamenRequestDTO(
                "Programación Java",
                "Examen básico",
                categoriaId,
                List.of(preguntaInvalida)
        );

        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));

        // Act & Assert
        assertThatThrownBy(() -> gestorExamen.crearExamen(requestDTO, usuarioId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Las opciones son obligatorias para MULTIPLE_CHOICE");

        verify(examenRepository, never()).save(any(Examen.class));
    }

    @Test
    void eliminarExamen_usuarioEsPropietario_eliminaExitosamente() {
        // Arrange
        int examenId = 100;
        int usuarioId = 1;
        String rol = "USER";

        Examen examenExistente = new Examen();
        examenExistente.setId(examenId);
        examenExistente.setCreadorId(usuarioId);

        when(examenRepository.findById(examenId)).thenReturn(Optional.of(examenExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, examenExistente.getCreadorId(), rol)).thenReturn(true);

        // Act
        gestorExamen.eliminarExamen(examenId, usuarioId, rol);

        // Assert
        verify(examenRepository, times(1)).findById(examenId);
        verify(gestorSeguridad, times(1)).esPropietarioOAdmin(usuarioId, examenExistente.getCreadorId(), rol);
        verify(examenRepository, times(1)).deleteById(examenId);
    }

    @Test
    void eliminarExamen_usuarioNoAutorizado_lanzaExcepcion() {
        // Arrange
        int examenId = 100;
        int usuarioIdIntruso = 99;
        String rol = "USER";

        Examen examenExistente = new Examen();
        examenExistente.setId(examenId);
        examenExistente.setCreadorId(1);

        when(examenRepository.findById(examenId)).thenReturn(Optional.of(examenExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioIdIntruso, examenExistente.getCreadorId(), rol)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> gestorExamen.eliminarExamen(examenId, usuarioIdIntruso, rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No tenés permiso para eliminar este examen");

        verify(examenRepository, never()).deleteById(anyInt());
    }

    @Test
    void obtenerPorSlugs_slugsValidos_retornaExamenResponseDTO() {
        // Arrange
        String categoriaSlug = "programacion";
        String examenSlug = "java-basico";

        Categoria categoria = new Categoria();
        categoria.setId(10);
        categoria.setSlug(categoriaSlug);

        Examen examen = new Examen();
        examen.setId(100);
        examen.setTitulo("Java Básico");
        examen.setSlug(examenSlug);
        examen.setCategoriaId(10);

        // Agregamos una pregunta para validar el mapeo
        PreguntaMultipleChoice pregunta = new PreguntaMultipleChoice();
        pregunta.setId(1);
        pregunta.setTexto("Test");
        pregunta.setOpciones(List.of("A", "B"));
        pregunta.setRespuestaCorrecta("A");
        pregunta.setExamen(examen);
        examen.agregarPregunta(pregunta);

        when(categoriaRepository.findBySlug(categoriaSlug)).thenReturn(Optional.of(categoria));
        when(examenRepository.findBySlugAndCategoriaId(examenSlug, categoria.getId())).thenReturn(Optional.of(examen));

        // Act
        ExamenResponseDTO resultado = gestorExamen.obtenerPorSlugs(categoriaSlug, examenSlug);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("Java Básico");
        assertThat(resultado.preguntas()).hasSize(1);

        verify(categoriaRepository, times(1)).findBySlug(categoriaSlug);
        verify(examenRepository, times(1)).findBySlugAndCategoriaId(examenSlug, categoria.getId());
    }

    @Test
    void obtenerPorSlugs_slugInvalido_lanzaExcepcion() {
        // Arrange
        String categoriaSlugInvalida = "esto no es un slug!!!";
        String examenSlug = "java-basico";

        // Act & Assert
        assertThatThrownBy(() -> gestorExamen.obtenerPorSlugs(categoriaSlugInvalida, examenSlug))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Slug de categoría inválido");

        verify(categoriaRepository, never()).findBySlug(anyString());
        verify(examenRepository, never()).findBySlugAndCategoriaId(anyString(), anyInt());
    }
}