package com.quizforge.backend.gestor;

import com.quizforge.backend.dto.CategoriaRequestDTO;
import com.quizforge.backend.dto.CategoriaResponseDTO;
import com.quizforge.backend.dto.ExamenResumenDTO;
import com.quizforge.backend.model.Categoria;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.repository.CategoriaRepository;
import com.quizforge.backend.repository.ExamenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class GestorCategoria {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final CategoriaRepository categoriaRepository;
    private final ExamenRepository examenRepository;
    private final GestorSeguridad gestorSeguridad;

    public GestorCategoria(CategoriaRepository categoriaRepository, ExamenRepository examenRepository, GestorSeguridad gestorSeguridad) {
        this.categoriaRepository = categoriaRepository;
        this.examenRepository = examenRepository;
        this.gestorSeguridad = gestorSeguridad;
    }

    @Transactional
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO dto, int usuarioId) {
        validarCategoriaRequest(dto);

        String slug = generarSlugUnico(dto.nombre());
        List<String> apodosNormalizados = normalizarApodos(dto.apodos());

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.nombre().trim());
        categoria.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        categoria.setSlug(slug);
        categoria.setApodos(apodosNormalizados);
        categoria.setCreadorId(usuarioId);

        return mapearAResponseDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminarCategoria(int categoriaId, int usuarioId, String rol) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada"
                ));

        if (!gestorSeguridad.esPropietarioOAdmin(usuarioId, categoria.getCreadorId(), rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para eliminar esta categoría"
            );
        }

        long cantidadExamenes = examenRepository.countByCategoriaId(categoriaId);
        if (cantidadExamenes > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede borrar una categoría que contiene exámenes"
            );
        }

        categoriaRepository.deleteById(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::mapearAResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamenResumenDTO> obtenerExamenesPorCategoria(String categoriaSlug) {
        validarSlug(categoriaSlug);

        Categoria categoria = categoriaRepository.findBySlug(categoriaSlug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada: " + categoriaSlug
                ));

        return examenRepository.findByCategoriaId(categoria.getId()).stream()
                .map(this::mapearAExamenResumenDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Categoria> buscarPorApodo(String apodo) {
        if (apodo == null || apodo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El apodo es obligatorio");
        }

        return categoriaRepository.findByApodo(apodo.trim().toLowerCase(Locale.ROOT));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarCategoriasPorCreador(int usuarioId) {
        return categoriaRepository.findCategoriasConExamenesDelUsuario(usuarioId).stream()
                .map(this::mapearAResponseDTO)
                .toList();
    }

    private void validarSlug(String slug) {
        if (slug == null || slug.isBlank() || !SLUG_PATTERN.matcher(slug).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug de categoría inválido: " + slug);
        }
    }

    private CategoriaResponseDTO mapearAResponseDTO(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getSlug(),
                List.copyOf(categoria.getApodos())
        );
    }

    private ExamenResumenDTO mapearAExamenResumenDTO(Examen examen) {
        return new ExamenResumenDTO(
                examen.getId(),
                examen.getTitulo(),
                examen.getDescripcion(),
                examen.getSlug(),
                examen.getCreadoEn()
        );
    }

    private void validarCategoriaRequest(CategoriaRequestDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
    }

    private List<String> normalizarApodos(List<String> apodos) {
        if (apodos == null || apodos.isEmpty()) {
            return List.of();
        }

        Set<String> apodosUnicos = new LinkedHashSet<>();
        for (String apodo : apodos) {
            if (apodo == null || apodo.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los apodos no pueden estar vacíos");
            }
            apodosUnicos.add(apodo.trim().toLowerCase(Locale.ROOT));
        }

        return new ArrayList<>(apodosUnicos);
    }

    private String generarSlugUnico(String nombre) {
        String baseSlug = generarSlug(nombre);
        String slug = baseSlug;
        int sufijo = 1;

        while (categoriaRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + sufijo++;
        }

        return slug;
    }

    private String generarSlug(String nombre) {
        String normalizado = java.text.Normalizer.normalize(nombre.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (normalizado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se pudo generar un slug válido a partir del nombre"
            );
        }

        return normalizado;
    }
}