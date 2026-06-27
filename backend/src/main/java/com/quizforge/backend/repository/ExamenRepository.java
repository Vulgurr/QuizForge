package com.quizforge.backend.repository;

import com.quizforge.backend.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Integer> {

    Optional<Examen> findBySlug(String slug);

    Optional<Examen> findBySlugAndCategoriaId(String slug, int categoriaId);

    List<Examen> findByCategoriaId(int categoriaId);
}
