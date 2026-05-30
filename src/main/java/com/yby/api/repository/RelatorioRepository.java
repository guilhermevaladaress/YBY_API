package com.yby.api.repository;

import com.yby.api.entity.Relatorio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {
    List<Relatorio> findByMunicipioIdOrderByDataReferenciaDesc(Long municipioId);
    Optional<Relatorio> findTopByMunicipioIdOrderByDataReferenciaDesc(Long municipioId);
    Page<Relatorio> findAllByOrderByCreatedAtDesc(Pageable pageable);
    void deleteByMunicipioId(Long municipioId);
}
