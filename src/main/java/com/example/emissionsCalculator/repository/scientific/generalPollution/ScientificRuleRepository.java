package com.example.emissionsCalculator.repository.scientific.generalPollution;

import com.example.emissionsCalculator.entity.scientific.generalPollution.ScientificRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <b>O que é:</b> Repositório JPA para a entidade ScientificRule.
 */
@Repository
public interface ScientificRuleRepository extends JpaRepository<ScientificRule, Long> {

    /**
     * Busca uma regra específica pelo seu ID textual (ex: CETESB_01).
     */
    Optional<ScientificRule> findByRuleId(String ruleId);

    /**
     * Lista todas as regras associadas a um poluente alvo específico.
     */
    List<ScientificRule> findByTargetPollutant(String targetPollutant);
}
