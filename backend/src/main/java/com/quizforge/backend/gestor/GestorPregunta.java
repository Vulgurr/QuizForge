package com.quizforge.backend.gestor;

import com.quizforge.backend.dto.PreguntaCreateDTO;
import com.quizforge.backend.dto.PreguntaDTO;
import com.quizforge.backend.dto.PreguntaDesarrolloDeterministicoDTO;
import com.quizforge.backend.dto.PreguntaDesarrolloNoDeterministicoDTO;
import com.quizforge.backend.dto.PreguntaMultipleChoiceDTO;
import com.quizforge.backend.dto.PreguntaVerdaderoFalsoDTO;
import com.quizforge.backend.model.Examen;
import com.quizforge.backend.model.pregunta.Pregunta;
import com.quizforge.backend.model.pregunta.PreguntaDesarrolloDeterministica;
import com.quizforge.backend.model.pregunta.PreguntaDesarrolloNoDeterministica;
import com.quizforge.backend.model.pregunta.PreguntaMultipleChoice;
import com.quizforge.backend.model.pregunta.PreguntaVerdaderoOFalso;
import com.quizforge.backend.repository.ExamenRepository;
import com.quizforge.backend.repository.PreguntaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GestorPregunta {

    private final PreguntaRepository preguntaRepository;
    private final ExamenRepository examenRepository;
    private final GestorSeguridad gestorSeguridad;

    public GestorPregunta(
            PreguntaRepository preguntaRepository,
            ExamenRepository examenRepository,
            GestorSeguridad gestorSeguridad
    ) {
        this.preguntaRepository = preguntaRepository;
        this.examenRepository = examenRepository;
        this.gestorSeguridad = gestorSeguridad;
    }

    @Transactional
    public PreguntaDTO crearPregunta(PreguntaCreateDTO dto, int usuarioId, String rol) {
        validarPreguntaCreateDTO(dto);

        Examen examen = examenRepository.findById(dto.examenId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Examen no encontrado: " + dto.examenId()
                ));

        if (!gestorSeguridad.esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para agregar preguntas a este examen"
            );
        }

        Pregunta pregunta = crearPreguntaDesdeDTO(dto);
        pregunta.setExamen(examen);
        Pregunta preguntaGuardada = preguntaRepository.save(pregunta);
        return mapearPreguntaADTO(preguntaGuardada);
    }

    @Transactional
    public void crearPreguntasEnMasa(List<PreguntaCreateDTO> dtos, int usuarioId, String rol) {
        if (dtos == null || dtos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La lista de preguntas no puede estar vacía");
        }

        for (PreguntaCreateDTO dto : dtos) {
            validarPreguntaCreateDTO(dto);

            Examen examen = examenRepository.findById(dto.examenId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Examen no encontrado: " + dto.examenId()
                    ));

            if (!gestorSeguridad.esPropietarioOAdmin(usuarioId, examen.getCreadorId(), rol)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tenés permiso para agregar preguntas a este examen"
                );
            }

            Pregunta pregunta = crearPreguntaDesdeDTO(dto);
            pregunta.setExamen(examen);
            preguntaRepository.save(pregunta);
        }
    }

    @Transactional
    public void eliminarPregunta(int preguntaId, int usuarioId, String rol) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pregunta no encontrada: " + preguntaId
                ));

        if (!gestorSeguridad.esPropietarioOAdmin(usuarioId, pregunta.getExamen().getCreadorId(), rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés permiso para eliminar esta pregunta"
            );
        }

        preguntaRepository.deleteById(preguntaId);
    }

    @Transactional(readOnly = true)
    public List<PreguntaDTO> listarPreguntasPorExamen(int examenId) {
        return preguntaRepository.findByExamenId(examenId).stream()
                .map(this::mapearPreguntaADTO)
                .toList();
    }

    private void validarPreguntaCreateDTO(PreguntaCreateDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DTO de pregunta no puede ser nulo");
        }
        if (dto.examenId() == null || dto.examenId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El examenId es obligatorio");
        }
        if (dto.texto() == null || dto.texto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El texto de la pregunta es obligatorio");
        }
        validarCamposEspecificos(dto);
    }

    private void validarCamposEspecificos(PreguntaCreateDTO dto) {
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

    private Pregunta crearPreguntaDesdeDTO(PreguntaCreateDTO dto) {
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
}
