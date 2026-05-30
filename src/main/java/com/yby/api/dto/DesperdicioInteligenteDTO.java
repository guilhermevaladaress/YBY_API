package com.yby.api.dto;

import com.yby.api.entity.enums.Bioma;
import com.yby.api.entity.enums.Porte;
import java.math.BigDecimal;

/**
 * Saida da RN-107-A (Alerta de Desperdicio Inteligente / benchmarking).
 *
 * <p>Compara o custo por unidade de resultado ambiental do municipio com a media do seu grupo
 * de semelhantes (mesmo bioma e porte). Custo superior a 2x a media do grupo gera alerta
 * vermelho de ineficiencia relativa (CF art. 37 - principio da eficiencia).</p>
 *
 * @param municipioId      municipio avaliado
 * @param nome             nome do municipio
 * @param bioma            bioma do grupo de comparacao
 * @param porte            porte do grupo de comparacao
 * @param gastoPublico     gasto publico no ano
 * @param resultadoAmbiental resultado ambiental no ano
 * @param custoPorResultado gasto / resultado (quanto menor, mais eficiente)
 * @param mediaGrupo       custo medio por resultado do grupo de semelhantes
 * @param razaoSobreMedia  custoPorResultado / mediaGrupo
 * @param nivelAlerta      verde | amarelo | vermelho
 * @param versaoAlgoritmo  versao do algoritmo (RN-300)
 */
public record DesperdicioInteligenteDTO(
    Long municipioId,
    String nome,
    Bioma bioma,
    Porte porte,
    BigDecimal gastoPublico,
    BigDecimal resultadoAmbiental,
    BigDecimal custoPorResultado,
    BigDecimal mediaGrupo,
    BigDecimal razaoSobreMedia,
    String nivelAlerta,
    String versaoAlgoritmo
) {
}
