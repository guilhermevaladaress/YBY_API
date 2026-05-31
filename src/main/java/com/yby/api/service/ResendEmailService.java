package com.yby.api.service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Envio de e-mail transacional via Resend (https://resend.com).
 *
 * <p>A API key vem por variavel de ambiente ({@code RESEND_API_KEY}) e NUNCA fica no
 * codigo. Se nao houver chave configurada, o envio e um no-op (retorna {@code false}),
 * permitindo que o ambiente local siga funcionando sem e-mail (fallback no fluxo de
 * recuperacao de senha).</p>
 */
@Service
public class ResendEmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final String apiKey;
    private final String from;

    public ResendEmailService(
        RestClient integracaoRestClient,
        @Value("${app.mail.resend-api-key:}") String apiKey,
        @Value("${app.mail.from:YBY <onboarding@resend.dev>}") String from) {
        this.restClient = integracaoRestClient;
        this.apiKey = apiKey;
        this.from = from;
    }

    /** Indica se o envio de e-mail esta configurado (chave presente). */
    public boolean habilitado() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Envia um e-mail HTML. Retorna {@code true} se o Resend aceitou a mensagem.
     * Nunca lanca: falhas sao logadas e retornam {@code false} (o chamador decide o fallback).
     */
    public boolean enviar(String para, String assunto, String html) {
        if (!habilitado()) {
            log.warn("Resend nao configurado (RESEND_API_KEY ausente); e-mail para {} nao enviado.", para);
            return false;
        }
        try {
            Map<String, Object> resp = restClient.post()
                .uri(RESEND_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("from", from, "to", List.of(para), "subject", assunto, "html", html))
                .retrieve()
                .body(Map.class);
            log.info("E-mail enviado via Resend para {} (id={}).", para, resp == null ? "?" : resp.get("id"));
            return true;
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail via Resend para {}: {}", para, e.getMessage());
            return false;
        }
    }

    /** Monta e envia o e-mail de recuperacao de senha com o link de redefinicao. */
    public boolean enviarRecuperacaoSenha(String para, String link, long minutosValidade) {
        String html = """
            <div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:auto;color:#1f2d27">
              <h2 style="color:#15803d">YBY - Recuperacao de senha</h2>
              <p>Recebemos um pedido para redefinir a sua senha de acesso ao YBY.</p>
              <p style="text-align:center;margin:28px 0">
                <a href="%s" style="background:#15803d;color:#fff;text-decoration:none;
                   padding:12px 24px;border-radius:8px;display:inline-block;font-weight:600">
                  Redefinir minha senha
                </a>
              </p>
              <p style="font-size:13px;color:#5c6b66">
                Ou copie e cole este link no navegador:<br>
                <a href="%s">%s</a>
              </p>
              <p style="font-size:13px;color:#5c6b66">O link expira em %d minutos. Se voce nao
                solicitou, ignore este e-mail.</p>
            </div>
            """.formatted(link, link, link, minutosValidade);
        return enviar(para, "YBY - Redefinicao de senha", html);
    }
}
