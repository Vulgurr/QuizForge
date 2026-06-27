package com.quizforge.backend.model.pregunta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@DiscriminatorValue("MULTIPLE_CHOICE")
@Getter
@Setter
@NoArgsConstructor
public class PreguntaMultipleChoice extends Pregunta {

    @ElementCollection
    @CollectionTable(
            name = "pregunta_opciones",
            joinColumns = @JoinColumn(name = "pregunta_id")
    )
    @Column(name = "opcion", nullable = false)
    private List<String> opciones;

    @Column(name = "respuesta_correcta_texto")
    private String respuestaCorrecta;
}