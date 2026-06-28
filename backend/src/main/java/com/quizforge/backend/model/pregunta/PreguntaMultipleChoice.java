package com.quizforge.backend.model.pregunta;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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
    @Column(name = "opcion")
    private List<String> opciones;

    @Column(name = "respuesta_correcta")
    private String respuestaCorrecta;
}