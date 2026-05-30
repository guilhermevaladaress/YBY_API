package com.yby.api.dto;

import com.yby.api.entity.enums.Semaforo;
import java.util.List;

public record ProntidaoDTO(
    Long municipioId,
    Semaforo semaforo,
    List<String> motivos
) {
}
