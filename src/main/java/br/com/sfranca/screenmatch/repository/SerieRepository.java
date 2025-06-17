package br.com.sfranca.screenmatch.repository;

import br.com.sfranca.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieRepository extends JpaRepository<Serie, Long> {
}
