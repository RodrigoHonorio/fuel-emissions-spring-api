package com.example.emissionsCalculator.repository.gasStation;

import com.example.emissionsCalculator.entity.gasStation.GasStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <b>O que é:</b> Repositório Spring Data JPA para a entidade GasStation[cite: 21].
 * <b>Para que serve:</b> Executar operações de escrita e leitura na tabela 'gas_stations' (OpenStreetMap)[cite: 19, 21].
 * <b>De onde veio:</b> Interface JpaRepository do Spring Framework[cite: 21].
 */
@Repository
public interface GasStationRepository extends JpaRepository<GasStation, Long> {
}