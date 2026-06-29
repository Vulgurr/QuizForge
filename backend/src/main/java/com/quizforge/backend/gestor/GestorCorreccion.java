package com.quizforge.backend.gestor;

import com.quizforge.backend.dto.CorreccionRequestDTO;
import com.quizforge.backend.dto.CorreccionResponseDTO;
import com.quizforge.backend.dto.RespuestaClienteDTO;
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

import java.util.HashSet;
import java.util.Set;

@Service
public class GestorCorreccion {

    private final ExamenRepository examenRepository;
    private final PreguntaRepository preguntaRepository;

    public GestorCorreccion(
            ExamenRepository examenRepository,
            PreguntaRepository preguntaRepository
    ) {
        this.examenRepository = examenRepository;
        this.preguntaRepository = preguntaRepository;
    }

    @Transactional(readOnly = true)
    public CorreccionResponseDTO calcularNotaExamen(CorreccionRequestDTO dto, int examenId) {
        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Examen no encontrado: " + examenId
                ));

        int totalPreguntas = examen.getPreguntas().size();
        if (totalPreguntas == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El examen no tiene preguntas para corregir"
            );
        }

        Set<Integer> preguntaIdsDelExamen = new HashSet<>();
        for (Pregunta p : examen.getPreguntas()) {
            preguntaIdsDelExamen.add(p.getId());
        }

        int correctas = 0;
        int respuestasConsideradas = 0;

        for (RespuestaClienteDTO respuesta : dto.respuestas()) {
            if (!preguntaIdsDelExamen.contains(respuesta.preguntaId())) {
                continue;
            }

            Pregunta pregunta = preguntaRepository.findById(respuesta.preguntaId())
                    .orElse(null);

            if (pregunta == null || !pregunta.getExamen().getId().equals(examenId)) {
                continue;
            }

            boolean esCorrecta = evaluarRespuesta(pregunta, respuesta.valorDichoPorElUsuario());
            if (esCorrecta) {
                correctas++;
            }
            respuestasConsideradas++;
        }

        double puntajeFinal = 0.0;
        if (respuestasConsideradas > 0) {
            puntajeFinal = ((double) correctas / respuestasConsideradas) * 10.0;
            puntajeFinal = Math.round(puntajeFinal * 100.0) / 100.0;
        }

        return new CorreccionResponseDTO(
                examenId,
                puntajeFinal,
                totalPreguntas,
                correctas
        );
    }

    private boolean evaluarRespuesta(Pregunta pregunta, String valorUsuario) {
        String valorNormalizado = valorUsuario.trim().toLowerCase();

        return switch (pregunta) {
            case PreguntaMultipleChoice mc -> {
                String respuestaCorrecta = mc.getRespuestaCorrecta().trim().toLowerCase();
                yield valorNormalizado.equals(respuestaCorrecta);
            }
            case PreguntaVerdaderoOFalso vf -> {
                boolean respuestaCorrecta = vf.isRespuestaCorrecta();
                boolean valorBooleano = parsearBooleano(valorNormalizado);
                yield valorBooleano == respuestaCorrecta;
            }
            case PreguntaDesarrolloDeterministica dd -> {
                String respuestaEsperada = dd.getRespuestaEsperadaExacta().trim().toLowerCase();
                yield valorNormalizado.equals(respuestaEsperada);
            }
            case PreguntaDesarrolloNoDeterministica dn -> {
                yield false;
            }
            default -> false;
        };
    }

    private boolean parsearBooleano(String valor) {
        if (valor.equals("true") || valor.equals("verdadero") || valor.equals("si") || valor.equals("sí")) {
            return true;
        }
        if (valor.equals("false") || valor.equals("falso") || valor.equals("no")) {
            return false;
        }
        return false;
    }
}
