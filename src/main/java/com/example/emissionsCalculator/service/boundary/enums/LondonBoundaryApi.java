package com.example.emissionsCalculator.service.boundary.enums;

import lombok.Getter;

/**
 * <b>O que é:</b> Enum de configuração para os endpoints e parâmetros da ONS (ArcGIS).
 * <b>Para que serve:</b> Centralizar URLs externas e filtros geográficos, evitando "magic strings" e facilitando manutenção caso a fonte mude.
 */
@Getter
public enum LondonBoundaryApi {

    ONS_ARCGIS_DISTRICTS(
            "https://services1.arcgis.com/ESMARspQHYMw9BZ9/arcgis/rest/services/Local_Authority_Districts_December_2022_UK_BFC_V2/FeatureServer/0/query",
            "LAD22CD LIKE 'E09%'", // Filtro específico para os 33 boroughs de Londres
            "4326",                 // Sistema de referência espacial WGS84 (latitude/longitude padrão)
            "geojson"               // Formato de resposta esperado
    );

    private final String baseUrl;
    private final String whereClause;
    private final String outSr;
    private final String format;

    LondonBoundaryApi(String baseUrl, String whereClause, String outSr, String format) {
        this.baseUrl = baseUrl;
        this.whereClause = whereClause;
        this.outSr = outSr;
        this.format = format;
    }
}
