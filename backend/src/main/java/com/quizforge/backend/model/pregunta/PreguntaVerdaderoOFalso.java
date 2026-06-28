package com.quizforge.backend.model.pregunta;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("VERDADERO_FALSO")
@Getter
@Setter
@NoArgsConstructor
public class PreguntaVerdaderoOFalso extends Pregunta {

    @Column(name = "respuesta_correcta")
    private boolean respuestaCorrecta;
}