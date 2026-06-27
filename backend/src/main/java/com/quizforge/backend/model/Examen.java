package com.quizforge.backend.model;

import com.quizforge.backend.model.pregunta.Pregunta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "examenes")
@Getter
@Setter
@NoArgsConstructor
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;

    private String descripcion;

    private String slug;

    private int creadorId;

    private int categoriaId;

    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();

    public void agregarPregunta(Pregunta pregunta) {
        preguntas.add(pregunta);
        pregunta.setExamen(this);
    }
}
