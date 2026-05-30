package com.yby.api.repository;

import com.yby.api.entity.Municipio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    Optional<Municipio> findByCodigoIbge(String codigoIbge);
    boolean existsByCodigoIbge(String codigoIbge);
}
