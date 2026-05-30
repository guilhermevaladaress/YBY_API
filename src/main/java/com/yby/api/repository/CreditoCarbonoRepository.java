package com.yby.api.repository;

import com.yby.api.entity.CreditoCarbono;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditoCarbonoRepository extends JpaRepository<CreditoCarbono, Long> {

    List<CreditoCarbono> findByMunicipioId(Long municipioId);
}
