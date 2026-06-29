package com.quizforge.backend.unit.gestor;

import com.quizforge.backend.dto.CategoriaRequestDTO;
import com.quizforge.backend.dto.CategoriaResponseDTO;
import com.quizforge.backend.dto.ExamenResumenDTO;
import com.quizforge.backend.gestor.GestorCategoria;
import com.quizforge.backend.gestor.GestorSeguridad;
import com.quizforge.backend.model.Categoria;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.repository.CategoriaRepository;
import com.quizforge.backend.repository.ExamenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorCategoriaTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private GestorSeguridad gestorSeguridad;

    @InjectMocks
    private GestorCategoria gestorCategoria;

    @Test
    void crearCategoria_datosValidos_retornaCategoriaResponseDTO() {
        // Arrange
        int usuarioId = 1;
        CategoriaRequestDTO requestDTO = new CategoriaRequestDTO("Matemáticas", "Álgebra y Geometría", List.of("mates", "math"));

        // Simulamos que el slug no existe para que no entre al while
        when(categoriaRepository.findBySlug("matematicas")).thenReturn(Optional.empty());

        // Simulamos el guardado en la base de datos
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria c = invocation.getArgument(0);
            c.setId(10); // Le asignamos un ID falso como haría la BD
            return c;
        });

        // Act
        CategoriaResponseDTO resultado = gestorCategoria.crearCategoria(requestDTO, usuarioId);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Matemáticas");
        assertThat(resultado.slug()).isEqualTo("matematicas");
        assertThat(resultado.apodos()).containsExactlyInAnyOrder("mates", "math");

        verify(categoriaRepository, times(1)).findBySlug("matematicas");
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void crearCategoria_nombreVacio_lanzaExcepcion() {
        // Arrange
        int usuarioId = 1;
        CategoriaRequestDTO requestDTO = new CategoriaRequestDTO("", "Sin nombre", List.of());

        // Act & Assert
        assertThatThrownBy(() -> gestorCategoria.crearCategoria(requestDTO, usuarioId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El nombre es obligatorio");

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void eliminarCategoria_usuarioEsPropietarioOAdmin_eliminaExitosamente() {
        // Arrange
        int categoriaId = 10;
        int usuarioId = 1;
        String rol = "USER";

        Categoria categoriaExistente = new Categoria();
        categoriaExistente.setId(categoriaId);
        categoriaExistente.setCreadorId(usuarioId);

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoriaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, categoriaExistente.getCreadorId(), rol)).thenReturn(true);

        // Act
        gestorCategoria.eliminarCategoria(categoriaId, usuarioId, rol);

        // Assert
        verify(categoriaRepository, times(1)).findById(categoriaId);
        verify(gestorSeguridad, times(1)).esPropietarioOAdmin(usuarioId, categoriaExistente.getCreadorId(), rol);
        verify(categoriaRepository, times(1)).deleteById(categoriaId);
    }

    @Test
    void eliminarCategoria_usuarioNoAutorizado_lanzaExcepcion() {
        // Arrange
        int categoriaId = 10;
        int usuarioIdIntruso = 99;
        String rol = "USER";

        Categoria categoriaExistente = new Categoria();
        categoriaExistente.setId(categoriaId);
        categoriaExistente.setCreadorId(1); // El dueño es el ID 1

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoriaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioIdIntruso, categoriaExistente.getCreadorId(), rol)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> gestorCategoria.eliminarCategoria(categoriaId, usuarioIdIntruso, rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No tienes permisos");

        verify(categoriaRepository, never()).deleteById(anyInt());
    }

    @Test
    void obtenerExamenesPorCategoria_categoriaValida_retornaListaDeResumenes() {
        // Arrange
        String slug = "programacion";
        Categoria categoria = new Categoria();
        categoria.setId(5);
        categoria.setSlug(slug);

        Examen examen1 = new Examen();
        examen1.setId(100);
        examen1.setTitulo("Java Basico");
        examen1.setSlug("java-basico");
        examen1.setCreadoEn(LocalDateTime.now());

        when(categoriaRepository.findBySlug(slug)).thenReturn(Optional.of(categoria));
        when(examenRepository.findByCategoriaId(categoria.getId())).thenReturn(List.of(examen1));

        // Act
        List<ExamenResumenDTO> resultados = gestorCategoria.obtenerExamenesPorCategoria(slug);

        // Assert
        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().titulo()).isEqualTo("Java Basico");
        assertThat(resultados.getFirst().slug()).isEqualTo("java-basico");

        verify(categoriaRepository, times(1)).findBySlug(slug);
        verify(examenRepository, times(1)).findByCategoriaId(categoria.getId());
    }

    @Test
    void buscarPorApodo_apodoValido_retornaListaDeCategorias() {
        // Arrange
        String apodo = " math ";
        Categoria categoria = new Categoria();
        categoria.setNombre("Matemáticas");

        // Se espera que el método internamente le haga trim() y toLowerCase()
        when(categoriaRepository.findByApodo("math")).thenReturn(List.of(categoria));

        // Act
        List<Categoria> resultados = gestorCategoria.buscarPorApodo(apodo);

        // Assert
        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getNombre()).isEqualTo("Matemáticas");

        verify(categoriaRepository, times(1)).findByApodo("math");
    }

    @Test
    void listarCategoriasPorCreador_usuarioConExamenes_retornaListaDeCategorias() {
        // Arrange
        int usuarioId = 1;
        Categoria categoria1 = new Categoria();
        categoria1.setId(1);
        categoria1.setNombre("Matemáticas");
        categoria1.setSlug("matematicas");

        Categoria categoria2 = new Categoria();
        categoria2.setId(2);
        categoria2.setNombre("Programación");
        categoria2.setSlug("programacion");

        when(categoriaRepository.findCategoriasConExamenesDelUsuario(usuarioId))
                .thenReturn(List.of(categoria1, categoria2));

        // Act
        List<CategoriaResponseDTO> resultados = gestorCategoria.listarCategoriasPorCreador(usuarioId);

        // Assert
        assertThat(resultados).hasSize(2);
        assertThat(resultados.get(0).nombre()).isEqualTo("Matemáticas");
        assertThat(resultados.get(1).nombre()).isEqualTo("Programación");

        verify(categoriaRepository, times(1)).findCategoriasConExamenesDelUsuario(usuarioId);
    }

    @Test
    void listarCategoriasPorCreador_usuarioSinExamenes_retornaListaVacia() {
        // Arrange
        int usuarioId = 99;
        when(categoriaRepository.findCategoriasConExamenesDelUsuario(usuarioId))
                .thenReturn(List.of());

        // Act
        List<CategoriaResponseDTO> resultados = gestorCategoria.listarCategoriasPorCreador(usuarioId);

        // Assert
        assertThat(resultados).isEmpty();

        verify(categoriaRepository, times(1)).findCategoriasConExamenesDelUsuario(usuarioId);
    }

    @Test
    void eliminarCategoria_conExamenesVinculados_lanzaExcepcionConflict() {
        // Arrange
        int categoriaId = 10;
        int usuarioId = 1;
        String rol = "USER";

        Categoria categoriaExistente = new Categoria();
        categoriaExistente.setId(categoriaId);
        categoriaExistente.setCreadorId(usuarioId);

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoriaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, categoriaExistente.getCreadorId(), rol)).thenReturn(true);
        when(examenRepository.countByCategoriaId(categoriaId)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> gestorCategoria.eliminarCategoria(categoriaId, usuarioId, rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No se puede borrar una categoría que contiene exámenes");

        verify(categoriaRepository, never()).deleteById(anyInt());
    }

    @Test
    void eliminarCategoria_sinExamenesVinculados_eliminaExitosamente() {
        // Arrange
        int categoriaId = 10;
        int usuarioId = 1;
        String rol = "USER";

        Categoria categoriaExistente = new Categoria();
        categoriaExistente.setId(categoriaId);
        categoriaExistente.setCreadorId(usuarioId);

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoriaExistente));
        when(gestorSeguridad.esPropietarioOAdmin(usuarioId, categoriaExistente.getCreadorId(), rol)).thenReturn(true);
        when(examenRepository.countByCategoriaId(categoriaId)).thenReturn(0L);

        // Act
        gestorCategoria.eliminarCategoria(categoriaId, usuarioId, rol);

        // Assert
        verify(categoriaRepository, times(1)).findById(categoriaId);
        verify(examenRepository, times(1)).countByCategoriaId(categoriaId);
        verify(categoriaRepository, times(1)).deleteById(categoriaId);
    }
}