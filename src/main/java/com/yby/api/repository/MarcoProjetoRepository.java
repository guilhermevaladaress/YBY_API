package com.yby.api.repository;

import com.yby.api.entity.MarcoProjeto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcoProjetoRepository extends JpaRepository<MarcoProjeto, Long> {

    List<MarcoProjeto> findByProjetoId(Long projetoId);
}
