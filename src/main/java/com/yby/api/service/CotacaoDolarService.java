package com.yby.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yby.api.dto.CotacaoDolarDTO;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Busca a cotação USD/BRL para uma data de referência via AwesomeAPI.
 *
 * <p>Para data de hoje: https://economia.awesomeapi.com.br/json/last/USD-BRL<br>
 * Para data histórica: https://economia.awesomeapi.com.br/json/daily/USD-BRL/1?start_date=YYYYMMDD&end_date=YYYYMMDD</p>
 *
 * <p>Em caso de falha de rede ou ausência de dados (feriados/fins de semana sem registro),
 * recua para a cotação atual. Se ambas falharem, usa a taxa de fallback.</p>
 */
@Service
public class CotacaoDolarService {

    private static final Logger log = LoggerFactory.getLogger(CotacaoDolarService.class);

    private static final String URL_ATUAL =
        "https://economia.awesomeapi.com.br/json/last/USD-BRL";
    private static final String URL_HISTORICA =
        "https://economia.awesomeapi.com.br/json/daily/USD-BRL/1?start_date=%s&end_date=%s";

    /** Taxa de fallback caso a API esteja indisponível. */
    static final BigDecimal TAXA_FALLBACK = new BigDecimal("5.70");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CotacaoDolarService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retorna a cotação USD/BRL para a data informada.
     * Se a data for nula ou hoje, busca a cotação atual.
     */
    public CotacaoDolarDTO buscar(LocalDate data) {
        LocalDate referencia = data == null ? LocalDate.now() : data;
        if (referencia.equals(LocalDate.now())) {
            return buscarAtual(referencia);
        }
        return buscarHistorico(referencia);
    }

    private CotacaoDolarDTO buscarAtual(LocalDate referencia) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_ATUAL))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            String bid = root.path("USDBRL").path("bid").asText();
            if (!bid.isBlank()) {
                return new CotacaoDolarDTO(referencia, new BigDecimal(bid), "AwesomeAPI");
            }
        } catch (Exception e) {
            log.warn("Falha ao buscar cotacao atual USD/BRL: {}", e.getMessage());
        }
        return new CotacaoDolarDTO(referencia, TAXA_FALLBACK, "fallback");
    }

    private CotacaoDolarDTO buscarHistorico(LocalDate data) {
        try {
            String dataFormatada = data.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String url = String.format(URL_HISTORICA, dataFormatada, dataFormatada);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            if (root.isArray() && !root.isEmpty()) {
                String bid = root.get(0).path("bid").asText();
                if (!bid.isBlank()) {
                    return new CotacaoDolarDTO(data, new BigDecimal(bid), "AwesomeAPI");
                }
            }
            // Fim de semana / feriado: sem dado histórico, usa cotação atual
            return buscarAtual(data);
        } catch (Exception e) {
            log.warn("Falha ao buscar cotacao historica USD/BRL para {}: {}", data, e.getMessage());
            return new CotacaoDolarDTO(data, TAXA_FALLBACK, "fallback");
        }
    }
}
