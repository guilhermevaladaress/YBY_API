package com.yby.api.repository;

import com.yby.api.entity.Desmatamento;
import com.yby.api.entity.enums.FonteDesmatamento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DesmatamentoRepository extends JpaRepository<Desmatamento, Long> {

    List<Desmatamento> findByMunicipioIdAndDataReferenciaBetween(Long municipioId, LocalDate dataInicio, LocalDate dataFim);

    List<Desmatamento> findByMunicipioIdAndFonteAndDataReferenciaBetween(Long municipioId,
                                                                          FonteDesmatamento fonte,
                                                                          LocalDate dataInicio,
                                                                          LocalDate dataFim);

    @Query(value = """
        select d.bioma as bioma,
               m.semaforo as semaforo,
               coalesce(sum(d.area_ha), 0) as area_total
        from desmatamento d
        join municipios m on m.id = d.municipio_id
        where extract(year from d.data_referencia) = :ano
        group by d.bioma, m.semaforo
        order by d.bioma, m.semaforo
    """, nativeQuery = true)
    List<Object[]> findResumoByAno(@Param("ano") Integer ano);

    @Query(value = """
        select coalesce(sum(d.area_ha), 0)
        from desmatamento d
        where extract(year from d.data_referencia) = :ano
    """, nativeQuery = true)
    java.math.BigDecimal findTotalByAno(@Param("ano") Integer ano);
}
