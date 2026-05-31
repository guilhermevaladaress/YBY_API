package com.yby.api.repository;

import com.yby.api.entity.EmissaoCarbono;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmissaoCarbonoRepository extends JpaRepository<EmissaoCarbono, Long> {

    @Query(value = """
        select e.municipio_id as municipioId,
               m.nome as nome,
               m.codigo_ibge as codigoIbge,
               coalesce(sum(e.emissao_tco2e), 0) as emissaoTotal,
               coalesce(avg(e.emissao_tco2e), 0) as mediaDiaria
        from emissoes_carbono e
        join municipios m on m.id = e.municipio_id
        where e.data_referencia between :dataInicio and :dataFim
        group by e.municipio_id, m.nome, m.codigo_ibge
        order by emissaoTotal asc
    """, nativeQuery = true)
    List<CarbonoHistoricoView> findHistoricoMenoresEmissoes(@Param("dataInicio") LocalDate dataInicio,
                                                            @Param("dataFim") LocalDate dataFim,
                                                            Pageable pageable);

    List<EmissaoCarbono> findByDataReferenciaBetween(LocalDate dataInicio, LocalDate dataFim);

    /** Data de referencia mais recente com emissao registrada (base para o filtro padrao da pagina). */
    @Query("select max(e.dataReferencia) from EmissaoCarbono e")
    LocalDate findMaxDataReferencia();

    List<EmissaoCarbono> findByMunicipioIdAndDataReferenciaBetween(Long municipioId,
                                                                   LocalDate dataInicio,
                                                                   LocalDate dataFim);

    void deleteByDataReferencia(LocalDate dataReferencia);
}
