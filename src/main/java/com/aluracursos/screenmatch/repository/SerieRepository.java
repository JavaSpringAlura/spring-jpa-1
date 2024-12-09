package com.aluracursos.screenmatch.repository;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie,Long> {
    
    Optional<Serie> findByTituloContainsIgnoreCase(String titulo);
    Optional<List<Serie>> findByTotalTemporadasGreaterThanEqual(Integer temporadas);
    List<Serie> findTop5ByOrderByEvaluacionDesc();
    List<Serie> findByGenero(Categoria categoria);
    Optional<List<Serie>> findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(Integer temporadas, Double evaluacion);

    // En JPQL se pone el nombre de la Clase y no de la tabla de la BD
    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :temporadas AND s.evaluacion >= :evaluacion")
    Optional<List<Serie>> seriesPorTemporadayEvaluacion(Integer temporadas, Double evaluacion);

    // SQL Normal: SELECT * FROM Series s JOIN Episodios e ON s.id = e.serie_id WHERE e.titulo ILIKE '%bald%';
    //ILIKE ignora mayusculas y minisculas
    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:titulo%")
    Optional<List<Episodio>> episodiosPorTitulo(String titulo);

    @Query("SELECT DISTINCT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.evaluacion DESC LIMIT 5")
    List<Episodio> top5Episodios(Serie serie);
}
