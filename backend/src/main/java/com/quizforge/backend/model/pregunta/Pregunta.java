package com.quizforge.backend.model.pregunta;

import com.quizforge.backend.model.Examen;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "preguntas")
@Getter
@Setter
@NoArgsConstructor
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;

    private String tipo;

    private String texto;

    @Column(name = "metadata_json")
    private String metadataJson;
}
