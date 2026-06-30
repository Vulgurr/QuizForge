package com.quizforge.backend.repository;

import com.quizforge.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findBySlug(String slug);

    // Búsqueda exacta
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.apodos a WHERE a = :apodo")
    List<Categoria> findByApodo(@Param("apodo") String apodo);

    // Búsqueda parcial (EL CORREGIDO)
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.apodos a WHERE LOWER(a) LIKE LOWER(CONCAT('%', :apodo, '%'))")
    List<Categoria> findByApodoContainingIgnoreCase(@Param("apodo") String apodo);

    @Query("SELECT DISTINCT c FROM Categoria c JOIN Examen e ON c.id = e.categoriaId WHERE e.creadorId = :usuarioId")
    List<Categoria> findCategoriasConExamenesDelUsuario(@Param("usuarioId") int usuarioId);
}