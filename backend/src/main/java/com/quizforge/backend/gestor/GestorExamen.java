package com.quizforge.backend.gestor;

import com.quizforge.backend.dto.ExamenRequestDTO;
import com.quizforge.backend.dto.ExamenResponseDTO;
import com.quizforge.backend.dto.PreguntaDTO;
import com.quizforge.backend.model.Categoria;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.model.pregunta.Pregunta;
import com.quizforge.backend.repository.CategoriaRepository;
import com.quizforge.backend.repository.ExamenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class GestorExamen {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final ExamenRepository examenRepository;
    private final CategoriaRepository categoriaRepository;
    private final GestorSeguridad gestorSeguridad;

    public GestorExamen(
            ExamenRepository examenRepository,
            CategoriaRepository categoriaRepository,
            GestorSeguridad gestorSeguridad
    ) {
        this.examenRepository = examenRepository;
        this.categoriaRepository = categoriaRepository;
        this.gestorSeguridad = gestorSeguridad;
    }

    @Transactional
    public ExamenResponseDTO crearExamen(ExamenRequestDTO dto, int usuarioId) {
        validarExamenRequest(dto);

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada: " + dto.categoriaId()
                ));

        String slug = generarSlugUnico(dto.titulo(), categoria.getId());

        Examen examen = new Examen();
        examen.setTitulo(dto.titulo().trim());
        examen.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        examen.setSlug(slug);
        examen.setCreadorId(usuarioId);
        examen.setCategoriaId(categoria.getId());
        examen.setCreadoEn(LocalDateTime.now());

        for (PreguntaDTO preguntaDto : dto.preguntas()) {
            validarPregunta(preguntaDto);
            Pregunta pregunta = new Pregunta();
            pregunta.setTipo(preguntaDto.tipo().trim());
            pregunta.setTexto(preguntaDto.texto().trim());
            pregunta.setMetadataJson(preguntaDto.metadataJson());
            examen.agregarPregunta(pregunta);
        }

        Examen examenGuardado = examenRepository.save(examen);
        return mapearAResponseDTO(examenGuardado);
    }

    @Transactional
    public void eliminarExamen(int examenId, int usuarioId, String rol) {
        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Examen no encontrado: " + examenId
                ));

        if (!gestorSeguridad.esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para eliminar este examen"
            );
        }

        examenRepository.deleteById(examenId);
    }

    @Transactional(readOnly = true)
    public ExamenResponseDTO obtenerPorSlugs(String categoriaSlug, String examenSlug) {
        validarSlug(categoriaSlug, "categoría");
        validarSlug(examenSlug, "examen");

        Categoria categoria = categoriaRepository.findBySlug(categoriaSlug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada: " + categoriaSlug
                ));

        Examen examen = examenRepository.findBySlugAndCategoriaId(examenSlug, categoria.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Examen no encontrado: " + examenSlug
                ));

        return mapearAResponseDTO(examen);
    }

    private void validarExamenRequest(ExamenRequestDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        if (dto.titulo() == null || dto.titulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título es obligatorio");
        }
        if (dto.categoriaId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador de categoría es inválido");
        }
        if (dto.preguntas() == null || dto.preguntas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El examen debe tener al menos una pregunta");
        }
    }

    private void validarPregunta(PreguntaDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pregunta no puede ser nula");
        }
        if (dto.tipo() == null || dto.tipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de pregunta es obligatorio");
        }
        if (dto.texto() == null || dto.texto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El texto de la pregunta es obligatorio");
        }
    }

    private void validarSlug(String slug, String entidad) {
        if (slug == null || slug.isBlank() || !SLUG_PATTERN.matcher(slug).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Slug de " + entidad + " inválido: " + slug
            );
        }
    }

    private String generarSlugUnico(String titulo, int categoriaId) {
        String baseSlug = generarSlug(titulo);
        String slug = baseSlug;
        int sufijo = 1;

        while (examenRepository.findBySlugAndCategoriaId(slug, categoriaId).isPresent()) {
            slug = baseSlug + "-" + sufijo++;
        }

        return slug;
    }

    private String generarSlug(String titulo) {
        String normalizado = titulo.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (normalizado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se pudo generar un slug válido a partir del título"
            );
        }

        return normalizado;
    }

    private ExamenResponseDTO mapearAResponseDTO(Examen examen) {
        List<PreguntaDTO> preguntas = examen.getPreguntas().stream()
                .map(p -> new PreguntaDTO(p.getTipo(), p.getTexto(), p.getMetadataJson()))
                .toList();

        return new ExamenResponseDTO(
                examen.getId(),
                examen.getTitulo(),
                examen.getDescripcion(),
                examen.getSlug(),
                examen.getCreadorId(),
                examen.getCategoriaId(),
                examen.getCreadoEn(),
                preguntas
        );
    }
}
