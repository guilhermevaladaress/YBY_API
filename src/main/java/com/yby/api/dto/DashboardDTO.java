package com.yby.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dados consolidados do dashboard JREDD+ Tocantins para o front-end.
 * Agrega KPIs, semaforos, alertas e rankings em uma unica chamada.
 */
public record DashboardDTO(
    // Visao geral do estado
    long totalMunicipios,
    long municipiosVerdes,
    long municipiosAmarelos,
    long municipiosVermelhos,

    // KPIs financeiros/ambientais
    BigDecimal kpiMedioRetorno,
    BigDecimal totalGastoPublicoReais,
    BigDecimal totalResultadoAmbiental,
    BigDecimal totalAreaDesmatadaHa12Meses,

    // Alertas e riscos
    long totalAlertasAtivos,
    long alertasCriticos,
    long focosQueimadaAnoCorrente,
    long focosQueimadaUltimos30Dias,

    // Rankings e prioridades
    List<MunicipioRankingDTO> top10Prioridade,
    List<MunicipioRankingDTO> top5Desperdicio,

    // Distribuicao por bioma (Cerrado e Amazonia no TO)
    Map<String, Long> municipiosPorBioma,

    // Metadados
    OffsetDateTime ultimaAtualizacao,
    String versaoAlgoritmo
) {}
