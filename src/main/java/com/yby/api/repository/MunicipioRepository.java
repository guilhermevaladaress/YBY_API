package com.yby.api.repository;

import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Semaforo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {

    Optional<Municipio> findByCodigoIbge(String codigoIbge);

    boolean existsByCodigoIbge(String codigoIbge);

    List<Municipio> findBySemaforo(Semaforo semaforo);

    @Query("select max(m.ultimaAtualizacao) from Municipio m")
    OffsetDateTime maxUltimaAtualizacao();
}
