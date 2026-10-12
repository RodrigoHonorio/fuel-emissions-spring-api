-- Garante que a extensão PostGIS está ativa no banco de dados zerado[cite: 1]
CREATE EXTENSION IF NOT EXISTS postgis;

-- =========================================================================
-- ÍNDICES ESPACIAIS DE ALTA PERFORMANCE (GIST)
-- =========================================================================

-- Índice GIST para os distritos de Londres (convertendo jsonb para geometria)
CREATE INDEX IF NOT EXISTS idx_london_boundaries_geojson
    ON london_boundaries USING GIST (ST_GeomFromGeoJSON(geojson_geometry::text));

-- Índice GIST espacial dinâmico para os postos de combustível (via latitude/longitude)
CREATE INDEX IF NOT EXISTS idx_cma_gas_stations_point
    ON cma_gas_stations USING GIST (ST_MakePoint(longitude, latitude));