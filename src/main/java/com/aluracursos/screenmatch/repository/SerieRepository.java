package com.aluracursos.screenmatch.repository;

import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie,Long> {
    
    Optional<Serie> findByTituloContainsIgnoreCase(String titulo);

}
