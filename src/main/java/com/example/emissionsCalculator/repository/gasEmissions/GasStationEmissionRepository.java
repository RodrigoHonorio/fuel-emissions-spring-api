package com.example.emissionsCalculator.repository.gasEmissions;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <b>O que é:</b> Repositório JPA para a entidade GasStationEmission.
 * <b>Para que serve:</b> Realizar operações de persistência e consultas na tabela 'gas_station_emissions'.
 */
@Repository
public interface GasStationEmissionRepository extends JpaRepository<GasStationEmission, Long> {

    List<GasStationEmission> findByGasStationIdOrderByCalculationTimestampDesc(Long gasStationId);

    @Query(value = """
        SELECT DISTINCT ON (gas_station_id) *
        FROM gas_station_emissions
        ORDER BY gas_station_id, calculation_timestamp DESC
        """, nativeQuery = true)
    List<GasStationEmission> findLatestEmissionsForAllStations();
}