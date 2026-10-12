package com.example.emissionsCalculator.repository.boundary;

import com.example.emissionsCalculator.entity.boundary.LondonBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <b>O que é:</b> Repositório JPA para a entidade LondonBoundary.
 * <b>Para que serve:</b> Consultar limites cadastrados pelo código do distrito para atualização idempotente.
 */
@Repository
public interface LondonBoundaryRepository extends JpaRepository<LondonBoundary, Long> {

    Optional<LondonBoundary> findByBoroughCode(String boroughCode);
}
