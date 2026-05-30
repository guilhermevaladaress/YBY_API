package com.yby.api.repository;

import com.yby.api.entity.Indicador;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicadorRepository extends JpaRepository<Indicador, Long> {
    Optional<Indicador> findTopByMunicipioIdOrderByAnoDesc(Long municipioId);
    Optional<Indicador> findByMunicipioIdAndAno(Long municipioId, Integer ano);
    List<Indicador> findByAno(Integer ano);
    List<Indicador> findByMunicipioIdOrderByAnoAsc(Long municipioId);
}
