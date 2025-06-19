package br.com.sfranca.screenmatch.repository;

import br.com.sfranca.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
       Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);
}
