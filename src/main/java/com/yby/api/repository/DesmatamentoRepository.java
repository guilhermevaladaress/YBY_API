package com.yby.api.repository;

import com.yby.api.entity.Desmatamento;
import com.yby.api.entity.enums.DesmatamentoFonte;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DesmatamentoRepository extends JpaRepository<Desmatamento, Long> {

    @Query("""
        select coalesce(sum(d.areaDesmatadaHa), 0)
        from Desmatamento d
        where d.municipio.id = :municipioId
        and d.dataReferencia between :inicio and :fim
        """)
    java.math.BigDecimal sumAreaByMunicipioAndPeriodo(
        @Param("municipioId") Long municipioId,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim
    );

    List<Desmatamento> findByMunicipioIdAndDataReferenciaBetweenOrderByDataReferenciaAsc(
        Long municipioId,
        LocalDate dataInicio,
        LocalDate dataFim
    );

    List<Desmatamento> findByMunicipioIdAndFonteAndDataReferenciaBetweenOrderByDataReferenciaAsc(
        Long municipioId,
        DesmatamentoFonte fonte,
        LocalDate dataInicio,
        LocalDate dataFim
    );

    List<Desmatamento> findByDataReferenciaBetween(LocalDate dataInicio, LocalDate dataFim);
}
