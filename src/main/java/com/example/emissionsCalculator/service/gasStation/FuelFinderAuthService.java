package com.example.emissionsCalculator.service.gasStation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * <b>O que é:</b> Serviço de autenticação OAuth2 Client Credentials do GOV.UK Fuel Finder[cite: 23].
 * <b>Para que serve:</b> Obter e armazenar em cache tokens Bearer JWT válidos para requisições na API oficial do governo britânico[cite: 23].
 * <b>De onde veio:</b> Servidor OAuth2 do GOV.UK Fuel Finder Service (`/oauth/token`)[cite: 23, 26].
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FuelFinderAuthService {

    /** RestClient do Spring Boot 3 para chamadas HTTP[cite: 24]. */
    private final RestClient restClient = RestClient.create();

    /** URL do servidor OAuth2 injetada do application.properties[cite: 23, 26]. */
    @Value("${fuelfinder.api.auth-url}")
    private String authUrl;

    /** Client ID oficial[cite: 23, 26]. */
    @Value("${fuelfinder.api.client-id}")
    private String clientId;

    /** Client Secret oficial[cite: 23, 26]. */
    @Value("${fuelfinder.api.client-secret}")
    private String clientSecret;

    /** Scope das permissões[cite: 23, 26]. */
    @Value("${fuelfinder.api.scope:fuelfinder.read}")
    private String scope;

    /** Token armazenado em memória[cite: 23]. */
    private String cachedToken;

    /** Timestamp de expiração do token ativo[cite: 23]. */
    private Instant tokenExpiration;

    /**
     * <b>O que é:</b> Obtenção de token de acesso[cite: 23].
     * <b>Para que serve:</b> Retorna o token em cache se válido ou faz nova requisição OAuth2[cite: 23].
     * <b>De onde veio:</b> Regra de negócio interna de autenticação[cite: 23].
     *
     * @return String contendo o Bearer Access Token.
     */
    public String getAccessToken() {
        if (cachedToken != null && tokenExpiration != null && Instant.now().isBefore(tokenExpiration.minusSeconds(60))) {
            return cachedToken;
        }

        log.info("🔑 A solicitar Token de Acesso OAuth2 ao Fuel Finder...");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("scope", scope);

        try {
            TokenResponse response = restClient.post()
                    .uri(authUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(TokenResponse.class);

            if (response != null && response.getAccessToken() != null) {
                this.cachedToken = response.getAccessToken();
                long expiresIn = response.getExpiresIn();
                this.tokenExpiration = Instant.now().plusSeconds(expiresIn);

                log.info("✅ Token obtido com sucesso (expira em {}s).", expiresIn);
                return this.cachedToken;
            }
            throw new RuntimeException("Resposta OAuth2 nula ou inválida.");

        } catch (HttpClientErrorException e) {
            log.error("❌ Erro da API ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha OAuth2: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("❌ Falha de ligação OAuth2: {}", e.getMessage());
            throw new RuntimeException("Erro ao conectar ao servidor OAuth2: " + e.getMessage(), e);
        }
    }

    /** DTO interno de resposta de Token[cite: 23]. */
    @Data
    private static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private long expiresIn;

        @JsonProperty("token_type")
        private String tokenType;
    }
}