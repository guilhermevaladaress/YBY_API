package com.yby.api.repository;

import com.yby.api.entity.InstituicaoCarbono;
import com.yby.api.entity.enums.InstituicaoTipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstituicaoCarbonoRepository extends JpaRepository<InstituicaoCarbono, Long> {

    List<InstituicaoCarbono> findByAtivoTrue();

    List<InstituicaoCarbono> findByTipoAndAtivoTrue(InstituicaoTipo tipo);

    List<InstituicaoCarbono> findByCompraJreddTrueAndAtivoTrue();
}
