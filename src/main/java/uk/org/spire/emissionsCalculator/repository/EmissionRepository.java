package uk.org.spire.emissionsCalculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;

import java.util.List;

/**
 * Repositório Spring Data JPA para gerenciamento e persistência de dados de emissão,
 * integrado com capacidades de análise geoespacial via PostGIS.
 */
@Repository
public interface EmissionRepository extends JpaRepository<PetrolStationEmission, Long> {

    /**
     * Realiza uma consulta espacial para encontrar registros de emissão dentro de um
     * determinado raio de distância (em metros) a partir de coordenadas geográficas de referência.
     *
     * Utiliza a função nativa do PostGIS `ST_DWithin` com projeção em geografia (SRID 4326),
     * garantindo precisão métrica para cálculos geodésicos.
     *
     * @param latitude         Latitude do ponto central de busca (ex: centro de monitoramento em Londres).
     * @param longitude        Longitude do ponto central de busca.
     * @param distanceInMeters Raio de alcance da busca em metros (ex: 5000 para 5km).
     * @return Lista de {@link PetrolStationEmission} localizadas dentro da área de influência da pluma.
     */
    @Query(value = "SELECT * FROM petrol_station_emissions e WHERE ST_DWithin(e.location::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :distanceInMeters)", nativeQuery = true)
    List<PetrolStationEmission> findEmissionsWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("distanceInMeters") double distanceInMeters
    );
}