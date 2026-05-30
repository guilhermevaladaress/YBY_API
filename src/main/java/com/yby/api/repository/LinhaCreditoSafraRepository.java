package com.yby.api.repository;

import com.yby.api.entity.LinhaCreditoSafra;
import com.yby.api.entity.enums.ProgramaSafra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinhaCreditoSafraRepository extends JpaRepository<LinhaCreditoSafra, Long> {

    List<LinhaCreditoSafra> findByAtivoTrue();

    List<LinhaCreditoSafra> findByProgramaAndAtivoTrue(ProgramaSafra programa);

    List<LinhaCreditoSafra> findByExigePraticaCarbonoTrueAndAtivoTrue();
}
