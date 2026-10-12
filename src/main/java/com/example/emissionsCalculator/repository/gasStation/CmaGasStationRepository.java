package com.example.emissionsCalculator.repository.gasStation;

import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <b>O que é:</b> Repositório Spring Data JPA para a entidade CmaGasStation[cite: 20].
 * <b>Para que serve:</b> Executar operações de CRUD e buscas customizadas na tabela 'cma_gas_stations'[cite: 18, 20].
 * <b>De onde veio:</b> Interface JpaRepository do Spring Framework[cite: 20].
 */
@Repository
public interface CmaGasStationRepository extends JpaRepository<CmaGasStation, Long> {

    /**
     * <b>O que é:</b> Consulta derivativa do Spring Data[cite: 20].
     * <b>Para que serve:</b> Buscar um posto da CMA pelo seu identificador único 'nodeId' para evitar duplicações[cite: 20, 22].
     * <b>De onde veio:</b> Mapeamento automático do Spring Data JPA[cite: 20].
     */
    Optional<CmaGasStation> findByNodeId(String nodeId);
}