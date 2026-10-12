package com.example.emissionsCalculator.dto.gasStation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * <b>O que é:</b> DTO (Data Transfer Object) de resposta da API do Overpass (OpenStreetMap)[cite: 17].
 * <b>Para que serve:</b> Transporta os dados brutos recebidos em JSON da API do OpenStreetMap para o backend Java sem depender de entidades de persistência[cite: 17].
 * <b>De onde veio:</b> Resposta HTTP JSON enviada pelos servidores da API pública Overpass (OpenStreetMap)[cite: 17, 24].
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OverpassResponseDTO(
        /**
         * <b>O que é:</b> Lista de elementos geográficos[cite: 17].
         * <b>Para que serve:</b> Armazena os nós (nodes) e caminhos (ways) dos postos de combustível[cite: 17, 24].
         * <b>De onde veio:</b> Campo 'elements' da raiz do JSON do Overpass[cite: 17].
         */
        List<Element> elements
) {

    /**
     * <b>O que é:</b> Representação DTO de um elemento geográfico individual (ponto de combustível)[cite: 17].
     * <b>Para que serve:</b> Mapear ID, coordenadas (latitude/longitude) e metadados (tags) de cada posto[cite: 17, 24].
     * <b>De onde veio:</b> Elemento dentro da lista 'elements' do JSON da Overpass[cite: 17].
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Element(
            /** ID único do nó ou caminho no OpenStreetMap[cite: 17, 19]. */
            Long id,
            /** Latitude exata (caso seja um nó)[cite: 17]. */
            Double lat,
            /** Longitude exata (caso seja um nó)[cite: 17]. */
            Double lon,
            /** Centro geográfico do polígono (caso seja uma área/way)[cite: 17]. */
            Center center,
            /** Mapa de chaves/valores com metadados do posto (ex: nome, marca, combustíveis)[cite: 17, 24]. */
            Map<String, String> tags
    ) {}

    /**
     * <b>O que é:</b> DTO auxiliar para centroide geográfico[cite: 17].
     * <b>Para que serve:</b> Extrai a latitude e longitude central de postos cadastrados como áreas/polígonos[cite: 17, 24].
     * <b>De onde veio:</b> Objeto 'center' do JSON da API Overpass[cite: 17].
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Center(
            /** Latitude do centroide[cite: 17]. */
            Double lat,
            /** Longitude do centroide[cite: 17]. */
            Double lon
    ) {}
}
