package com.example.emissionsCalculator.entity.scientific.generalPollution;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "scientific_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScientificRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", unique = true, nullable = false, length = 100)
    private String ruleId;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "article_name", nullable = false, length = 255)
    private String articleName;

    // Correção para o PostgreSQL BYTEA no Hibernate 6+
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "pdf_content", columnDefinition = "BYTEA")
    private byte[] pdfContent;

    @Column(name = "pdf_file_name", length = 255)
    private String pdfFileName;

    @Column(name = "target_pollutant", nullable = false, length = 50)
    private String targetPollutant;

    @Column(name = "input_variables", nullable = false, length = 500)
    private String inputVariables;

    @Column(name = "mvel_formula", nullable = false, columnDefinition = "TEXT")
    private String mvelFormula;

    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}