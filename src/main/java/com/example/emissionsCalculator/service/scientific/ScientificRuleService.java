package com.example.emissionsCalculator.service.scientific;

import com.example.emissionsCalculator.dto.scientific.ScientificRuleResponseDTO;
import com.example.emissionsCalculator.entity.scientific.generalPollution.ScientificRule;
import com.example.emissionsCalculator.repository.scientific.generalPollution.ScientificRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScientificRuleService {

    private final ScientificRuleRepository ruleRepository;

    @Transactional
    public int processCsvAndPdfUpload(MultipartFile csvFile, MultipartFile pdfFile) {
        // 1. Validação prévia de segurança dos tipos de ficheiro
        validateFilesSecurity(csvFile, pdfFile);

        int count = 0;
        try {
            byte[] pdfBytes = (pdfFile != null && !pdfFile.isEmpty()) ? pdfFile.getBytes() : null;
            String pdfName = (pdfFile != null) ? pdfFile.getOriginalFilename() : null;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
                String line = br.readLine(); // Lê e ignora o cabeçalho
                if (line == null) return 0;

                while ((line = br.readLine()) != null) {
                    String[] cols = line.split("[,;]", -1);
                    if (cols.length < 7) continue;

                    String ruleId = cols[0].trim();
                    String mvelFormula = cols[5].trim();

                    // 2. Validação rigorosa de código hostil na fórmula MVEL
                    validateMvelFormulaSafety(mvelFormula);

                    ScientificRule rule = ruleRepository.findByRuleId(ruleId).orElse(new ScientificRule());
                    rule.setRuleId(ruleId);
                    rule.setRuleName(cols[1].trim());
                    rule.setArticleName(cols[2].trim());
                    rule.setTargetPollutant(cols[3].trim());
                    rule.setInputVariables(cols[4].trim());
                    rule.setMvelFormula(mvelFormula);
                    rule.setUnitOfMeasure(cols[6].trim());

                    if (cols.length > 7) {
                        rule.setDescription(cols[7].trim());
                    }

                    if (pdfBytes != null) {
                        rule.setPdfContent(pdfBytes);
                        rule.setPdfFileName(pdfName);
                    }

                    ruleRepository.save(rule);
                    count++;
                }
            }
            log.info("✅ Sucesso: {} regras científicas validadas e importadas com segurança!", count);
            return count;

        } catch (SecurityException se) {
            log.error("🚨 ALERTA DE SEGURANÇA: Tentativa de upload de conteúdo hostil detetada: {}", se.getMessage());
            throw se;
        } catch (Exception e) {
            log.error("❌ Erro ao processar ficheiros científicos: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na importação de regras: " + e.getMessage());
        }
    }

    private void validateFilesSecurity(MultipartFile csvFile, MultipartFile pdfFile) {
        if (csvFile == null || csvFile.isEmpty()) {
            throw new IllegalArgumentException("O ficheiro CSV de regras é obrigatório.");
        }

        String csvName = csvFile.getOriginalFilename();
        if (csvName == null || (!csvName.toLowerCase().endsWith(".csv") && !csvName.toLowerCase().endsWith(".txt"))) {
            throw new SecurityException("Formato de ficheiro inválido. Apenas ficheiros .csv são permitidos.");
        }

        if (pdfFile != null && !pdfFile.isEmpty()) {
            String pdfName = pdfFile.getOriginalFilename();
            if (pdfName == null || !pdfName.toLowerCase().endsWith(".pdf")) {
                throw new SecurityException("Formato de artigo inválido. Apenas ficheiros .pdf são permitidos.");
            }
        }
    }

    private void validateMvelFormulaSafety(String formula) {
        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException("A fórmula MVEL não pode estar vazia.");
        }

        String lowerFormula = formula.toLowerCase();

        List<String> dangerousKeywords = List.of(
                "runtime", "processbuilder", "system", "class",
                "constructor", "eval", "exec", "file", "thread", "reflect"
        );

        for (String keyword : dangerousKeywords) {
            if (lowerFormula.contains(keyword)) {
                throw new SecurityException("A expressão MVEL contém o termo restrito/potencialmente hostil: '" + keyword + "'");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ScientificRuleResponseDTO> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(rule -> new ScientificRuleResponseDTO(
                        rule.getId(),
                        rule.getRuleId(),
                        rule.getRuleName(),
                        rule.getArticleName(),
                        rule.getPdfFileName(),
                        rule.getPdfContent() != null && rule.getPdfContent().length > 0,
                        rule.getTargetPollutant(),
                        rule.getInputVariables(),
                        rule.getMvelFormula(),
                        rule.getUnitOfMeasure(),
                        rule.getDescription(),
                        rule.getCreatedAt()
                ))
                .toList();
    }
}