package com.quizforge.backend.model.pregunta;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("DESARROLLO_NO_DETERMINISTICO")
@Getter
@Setter
@NoArgsConstructor
public class PreguntaDesarrolloNoDeterministica extends Pregunta {

    @Column(name = "rubrica_evaluacion", columnDefinition = "TEXT")
    private String rubricaEvaluacion;
}