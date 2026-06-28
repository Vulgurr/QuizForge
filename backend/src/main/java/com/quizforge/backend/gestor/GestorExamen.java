package com.quizforge.backend.gestor;

import com.quizforge.backend.dto.ExamenRequestDTO;
import com.quizforge.backend.dto.ExamenResponseDTO;
import com.quizforge.backend.dto.PreguntaDTO;
import com.quizforge.backend.dto.PreguntaMultipleChoiceDTO;
import com.quizforge.backend.dto.PreguntaVerdaderoFalsoDTO;
import com.quizforge.backend.dto.PreguntaDesarrolloDeterministicoDTO;
import com.quizforge.backend.dto.PreguntaDesarrolloNoDeterministicoDTO;
import com.quizforge.backend.model.Categoria;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.model.pregunta.Pregunta;
import com.quizforge.backend.model.pregunta.PreguntaMultipleChoice;
import com.quizforge.backend.model.pregunta.PreguntaVerdaderoOFalso;
import com.quizforge.backend.model.pregunta.PreguntaDesarrolloDeterministica;
import com.quizforge.backend.model.pregunta.PreguntaDesarrolloNoDeterministica;
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
            Pregunta pregunta = crearPreguntaDesdeDTO(preguntaDto);
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
        if (dto.texto() == null || dto.texto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El texto de la pregunta es obligatorio");
        }
        validarCamposEspecificos(dto);
    }

    private void validarCamposEspecificos(PreguntaDTO dto) {
        switch (dto) {
            case PreguntaMultipleChoiceDTO mc -> {
                if (mc.opciones() == null || mc.opciones().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las opciones son obligatorias para MULTIPLE_CHOICE");
                }
                if (mc.respuestaCorrecta() == null || mc.respuestaCorrecta().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La respuesta correcta es obligatoria para MULTIPLE_CHOICE");
                }
            }
            case PreguntaVerdaderoFalsoDTO vf -> {
                // No hay campos adicionales para validar
            }
            case PreguntaDesarrolloDeterministicoDTO dd -> {
                if (dd.respuestaEsperadaExacta() == null || dd.respuestaEsperadaExacta().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La respuesta esperada es obligatoria para DESARROLLO_DETERMINISTICO");
                }
            }
            case PreguntaDesarrolloNoDeterministicoDTO dn -> {
                if (dn.rubricaEvaluacion() == null || dn.rubricaEvaluacion().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La rúbrica de evaluación es obligatoria para DESARROLLO_NO_DETERMINISTICO");
                }
            }
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
        String normalizado = java.text.Normalizer.normalize(titulo.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
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
                .map(this::mapearPreguntaADTO)
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

    private PreguntaDTO mapearPreguntaADTO(Pregunta pregunta) {
        return switch (pregunta) {
            case PreguntaMultipleChoice mc -> new PreguntaMultipleChoiceDTO(
                    mc.getId(),
                    mc.getTexto(),
                    mc.getOpciones(),
                    mc.getRespuestaCorrecta(),
                    mc.getExamen().getId()
            );
            case PreguntaVerdaderoOFalso vf -> new PreguntaVerdaderoFalsoDTO(
                    vf.getId(),
                    vf.getTexto(),
                    vf.isRespuestaCorrecta(),
                    vf.getExamen().getId()
            );
            case PreguntaDesarrolloDeterministica dd -> new PreguntaDesarrolloDeterministicoDTO(
                    dd.getId(),
                    dd.getTexto(),
                    dd.getRespuestaEsperadaExacta(),
                    dd.getExamen().getId()
            );
            case PreguntaDesarrolloNoDeterministica dn -> new PreguntaDesarrolloNoDeterministicoDTO(
                    dn.getId(),
                    dn.getTexto(),
                    dn.getRubricaEvaluacion(),
                    dn.getExamen().getId()
            );
            default -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Tipo de pregunta no reconocido");
        };
    }

    private Pregunta crearPreguntaDesdeDTO(PreguntaDTO dto) {
        return switch (dto) {
            case PreguntaMultipleChoiceDTO mc -> {
                PreguntaMultipleChoice pregunta = new PreguntaMultipleChoice();
                pregunta.setTexto(mc.texto().trim());
                pregunta.setOpciones(mc.opciones());
                pregunta.setRespuestaCorrecta(mc.respuestaCorrecta().trim());
                yield pregunta;
            }
            case PreguntaVerdaderoFalsoDTO vf -> {
                PreguntaVerdaderoOFalso pregunta = new PreguntaVerdaderoOFalso();
                pregunta.setTexto(vf.texto().trim());
                pregunta.setRespuestaCorrecta(vf.respuestaCorrecta());
                yield pregunta;
            }
            case PreguntaDesarrolloDeterministicoDTO dd -> {
                PreguntaDesarrolloDeterministica pregunta = new PreguntaDesarrolloDeterministica();
                pregunta.setTexto(dd.texto().trim());
                pregunta.setRespuestaEsperadaExacta(dd.respuestaEsperadaExacta().trim());
                yield pregunta;
            }
            case PreguntaDesarrolloNoDeterministicoDTO dn -> {
                PreguntaDesarrolloNoDeterministica pregunta = new PreguntaDesarrolloNoDeterministica();
                pregunta.setTexto(dn.texto().trim());
                pregunta.setRubricaEvaluacion(dn.rubricaEvaluacion().trim());
                yield pregunta;
            }
        };
    }
}
