package com.yby.api.repository;

import com.yby.api.entity.ProjecaoInteligencia;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjecaoInteligenciaRepository extends JpaRepository<ProjecaoInteligencia, Long> {

    Optional<ProjecaoInteligencia> findByMunicipioId(Long municipioId);
}
