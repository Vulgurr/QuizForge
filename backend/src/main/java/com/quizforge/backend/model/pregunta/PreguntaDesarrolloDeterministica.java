package com.quizforge.backend.model.pregunta;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("DESARROLLO_DETERMINISTICO")
@Getter
@Setter
@NoArgsConstructor
public class PreguntaDesarrolloDeterministica extends Pregunta {

    @Column(name = "respuesta_esperada_exacta")
    private String respuestaEsperadaExacta;
}