package com.quizforge.backend.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    private String descripcion;

    private String slug;

    @Column(name = "creador_id", nullable = false)
    private Integer creadorId;
    @ElementCollection
    @CollectionTable(
            name = "categoria_apodos",
            joinColumns = @JoinColumn(name = "categoria_id")
    )
    @Column(name = "apodo", nullable = false)
    private List<String> apodos = new ArrayList<>();
}
