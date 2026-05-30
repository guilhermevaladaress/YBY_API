package com.yby.api.repository;

import com.yby.api.entity.Indicador;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndicadorRepository extends JpaRepository<Indicador, Long> {

    Optional<Indicador> findByMunicipioIdAndAno(Long municipioId, Integer ano);

    List<Indicador> findByMunicipioIdAndAnoBetweenOrderByAnoAsc(Long municipioId, Integer anoInicio, Integer anoFim);

    List<Indicador> findByAno(Integer ano);

    @Query("""
        select sum(i.gastoPublico), sum(i.resultadoAmbiental)
        from Indicador i
        where i.municipio.id = :municipioId
          and i.ano between :anoInicio and :anoFim
    """)
    List<Object[]> aggregateByMunicipioAndPeriodo(@Param("municipioId") Long municipioId,
                                                  @Param("anoInicio") Integer anoInicio,
                                                  @Param("anoFim") Integer anoFim);

    @Query("""
        select i.gastoPublico
        from Indicador i
        where i.ano = :ano and i.gastoPublico is not null
    """)
    List<BigDecimal> findGastosByAno(@Param("ano") Integer ano);

    @Query("""
        select i.resultadoAmbiental
        from Indicador i
        where i.ano = :ano and i.resultadoAmbiental is not null
    """)
    List<BigDecimal> findResultadosByAno(@Param("ano") Integer ano);
}
