package com.yby.api.repository;

import com.yby.api.entity.Alerta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByMunicipioIdAndAtivoTrue(Long municipioId);
}
