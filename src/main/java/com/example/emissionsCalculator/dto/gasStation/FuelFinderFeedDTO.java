package com.example.emissionsCalculator.dto.gasStation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <b>O que é:</b> DTO para desserialização do feed de dados oficiais do Fuel Finder (GOV.UK)[cite: 16].
 * <b>Para que serve:</b> Recebe os preços de combustíveis atualizados e localização dos postos direto do governo britânico[cite: 16].
 * <b>De onde veio:</b> Endpoint oficial da API do GOV.UK Fuel Finder Service[cite: 16, 26].
 */
@Data
public class FuelFinderFeedDTO {

    /**
     * <b>O que é:</b> Timestamp da última atualização do feed[cite: 16].
     * <b>Para que serve:</b> Controlo de versão e auditoria dos dados de preços recebidos[cite: 16].
     * <b>De onde veio:</b> Atributo 'last_updated' da raiz do JSON da API Fuel Finder[cite: 16].
     */
    @JsonProperty("last_updated")
    private String lastUpdated;

    /**
     * <b>O que é:</b> Lista de DTOs de postos de combustível[cite: 16].
     * <b>Para que serve:</b> Agrupa os postos e os respetivos preços retornados no feed[cite: 16].
     * <b>De onde veio:</b> Lista 'stations' do JSON da API Fuel Finder[cite: 16].
     */
    private List<StationDTO> stations;

    /**
     * <b>O que é:</b> DTO que representa um posto individual no feed do Fuel Finder[cite: 16].
     * <b>Para que serve:</b> Mapear id do site, marca, endereço, código postal e mapa dinâmico de preços[cite: 16].
     * <b>De onde veio:</b> Objetos internos da lista 'stations'[cite: 16].
     */
    @Data
    public static class StationDTO {
        /** ID único do estabelecimento na base governamental[cite: 16]. */
        @JsonProperty("site_id")
        private String siteId;

        /** Nome da marca Comercial (ex: Shell, BP, Esso)[cite: 16]. */
        private String brand;

        /** Endereço físico[cite: 16]. */
        private String address;

        /** Código postal britânico[cite: 16]. */
        private String postcode;

        /** Objeto DTO com as coordenadas geográficas[cite: 16]. */
        private LocationDTO location;

        /** Mapa dinâmico de combustível -> preço em pence (ex: "E10" -> 142.9)[cite: 16]. */
        private Map<String, Object> prices;
    }

    /**
     * <b>O que é:</b> DTO auxiliar para tratamento de coordenadas geográficas[cite: 16].
     * <b>Para que serve:</b> Converte dados heterogéneos de latitude e longitude em valores numéricos do tipo Double[cite: 16].
     * <b>De onde veio:</b> Objeto 'location' retornado dentro de cada 'station' no JSON da API[cite: 16].
     */
    @Data
    public static class LocationDTO {
        /** Valor de latitude genérico (pode vir como String ou Number no JSON)[cite: 16]. */
        private Object latitude;

        /** Valor de longitude genérico (pode vir como String ou Number no JSON)[cite: 16]. */
        private Object longitude;

        /**
         * <b>O que é:</b> Método utilitário de conversão[cite: 16].
         * <b>Para que serve:</b> Converte o valor genérico de latitude para Double com tratamento seguro de erros[cite: 16].
         * <b>De onde veio:</b> Lógica interna do DTO[cite: 16].
         */
        public Double getLatitudeAsDouble() {
            if (latitude == null) return null;
            try {
                return Double.parseDouble(latitude.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /**
         * <b>O que é:</b> Método utilitário de conversão[cite: 16].
         * <b>Para que serve:</b> Converte o valor genérico de longitude para Double com tratamento seguro de erros[cite: 16].
         * <b>De onde veio:</b> Lógica interna do DTO[cite: 16].
         */
        public Double getLongitudeAsDouble() {
            if (longitude == null) return null;
            try {
                return Double.parseDouble(longitude.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}