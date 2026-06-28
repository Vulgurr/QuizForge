package com.quizforge.backend.repository;

import com.quizforge.backend.model.pregunta.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    List<Pregunta> findByExamenId(Integer examenId);

    Optional<Pregunta> findByIdAndExamenId(Integer preguntaId, Integer examenId);
}
