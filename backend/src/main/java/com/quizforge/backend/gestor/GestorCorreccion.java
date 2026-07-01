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
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GestorCorreccion {

    private final ExamenRepository examenRepository;

    public GestorCorreccion(ExamenRepository examenRepository) {
        this.examenRepository = examenRepository;
    }

    @Transactional(readOnly = true)
    public CorreccionResponseDTO calcularNotaExamen(CorreccionRequestDTO dto, int examenId) {
        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Examen no encontrado"));
        int totalPreguntas = examen.getPreguntas().size();
        int correctas = 0;
        int respondidas = 0;

        for (RespuestaClienteDTO respuesta : dto.respuestas()) {
            // Buscamos la pregunta directamente en el examen por su ID
            Pregunta pregunta = examen.getPreguntas().stream()
                    .filter(p -> p.getId().equals(respuesta.preguntaId()))
                    .findFirst()
                    .orElse(null);

            if (pregunta != null) {
                respondidas++; // Contamos que la pregunta existía
                if (evaluarRespuesta(pregunta, respuesta.valorDichoPorElUsuario())) {
                    correctas++;
                }
            }
        }

        double puntajeFinal = totalPreguntas > 0 ? ((double) correctas / totalPreguntas) * 10.0 : 0.0;
        return new CorreccionResponseDTO(examenId, Math.round(puntajeFinal * 100.0) / 100.0, totalPreguntas, correctas);
    }

    private boolean evaluarRespuesta(Pregunta pregunta, String valorUsuario) {
        if (valorUsuario == null || valorUsuario.isBlank()) return false;

        String valorNormalizado = valorUsuario.trim().toLowerCase();

        // QUITAMOS LA MÁSCARA: Forzamos a Hibernate a revelar la clase real (subclase)
        Pregunta preguntaReal = (Pregunta) Hibernate.unproxy(pregunta);

        return switch (preguntaReal) {
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
            case PreguntaDesarrolloNoDeterministica dn -> false;
            default -> {
                yield false;
            }
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