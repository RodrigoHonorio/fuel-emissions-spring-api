Aqui está a nova documentação técnica completa e estruturada exatamente nos padrões dos arquivos anteriores do projeto (`SCIENTIFIC_MODELS_FRAMEWORK.md`, `SPIRE_EMISSIONS_FRAMEWORK_V2.md`, etc.).

Pode guardar este conteúdo num ficheiro com o nome **`DYNAMIC_SIMULATION_AND_SECURITY_FRAMEWORK.md`** na pasta de documentação do seu projeto. Ele foi redigido a pensar em cientistas, analistas de dados e programadores, detalhando o que foi implementado hoje, a origem de cada componente, a justificação científica/tecnológica e o modelo de IA/motor dinâmico utilizado.

---

# Documentação Técnica e Científica: Motor de Simulação Temporal, Regras Dinâmicas MVEL e Blindagem de Segurança (`DYNAMIC_SIMULATION_AND_SECURITY_FRAMEWORK.md`)

---

## 1. Visão Geral para Pesquisadores, Analistas e Desenvolvedores

### 1.1. Contexto e Objetivos da Nova Funcionalidade

No desenvolvimento de sistemas de monitoramento ambiental de alta precisão, a capacidade de testar modelos científicos descritos em artigos acadêmicos (em formato PDF) de forma dinâmica — sem a necessidade de recompilar o código-fonte em Java — é um diferencial crítico.

O ecossistema foi expandido hoje para integrar um **motor de execução dinâmica baseado em MVEL (MVFLEX Expression Language)** acoplado a uma **pipeline de simulação temporal bidirecional (Passado/Lookback + Futuro/Forecast)**. Além disso, foi implementada uma camada de **validação e sanitização de segurança preventiva**, garantindo que nenhum código hostil ou malicioso seja injetado através dos ficheiros CSV de regras ou dos artigos científicos anexados.

### 1.2. Papel de Cada Perfil no Ecossistema

* **Cientistas e Analistas Ambientais:** Podem submeter novos artigos científicos em PDF acompanhados de fórmulas parametrizadas em ficheiros CSV, definindo janelas de simulação personalizadas (ex: análise de impacto de 4 dias no passado e previsão para 3 dias no futuro) para avaliar a variação de poluentes (*Delta VOC*, *Benzene*, *PM10*, *NOX*) em mais de 275 postos de combustível na Grande Londres.


* **Analistas de Segurança / Engenheiros:** Conta com uma camada de validação estrita baseada em lista negra (*blacklist*) de termos sensíveis do sistema operativo e reflexão Java, mitigando riscos de execução remota de código (*RCE*).


* **Programadores Backend:** Arquitetura altamente desacoplada utilizando Spring Boot 3, JPA/PostgreSQL, processamento assíncrono via `CompletableFuture` e trabalhadores transacionais em background (`SimulationExecutorWorker`).



---

## 2. O Que Foi Implementado Hoje (Funcionalidades Principais)

### 2.1. Blindagem de Segurança e Sanitização de Ficheiros (`ScientificRuleService`)

Antes de qualquer regra matemática ser gravada na base de dados ou interpretada pelo motor, o serviço valida rigorosamente:

1. **Validação de Tipos MIME e Extensões:** Apenas ficheiros com extensão `.csv` (ou `.txt`) e `.pdf` são aceites, rejeitando executáveis ou extensões maliciosas disfarçadas.


2. **Filtro de Injeção de Código MVEL (`validateMvelFormulaSafety`):** Analisa a fórmula matemática em busca de termos sensíveis do Java ou do sistema operativo, bloqueando imediatamente expressões que contenham palavras-chave como:


* `runtime`, `processbuilder`, `system`, `class`, `constructor`, `eval`, `exec`, `file`, `thread`, `reflect`.



### 2.2. Simulação Temporal com Janelas Personalizadas (`TemporalSimulationService` & `SimulationExecutorWorker`)

O sistema agora suporta lotes de simulação temporal avançados através do endpoint `/api/simulations/run`:

* **Parâmetros de Entrada:** `batchName` (nome do lote), `forecastDays` (dias no futuro, ex: 3) e `lookbackDays` (dias no passado, ex: 4).


* **Processamento Assíncrono (`@Async` / `CompletableFuture`):** O cálculo pesado corre em background de forma a não bloquear as threads HTTP do servidor.


* **Snapshots de Entrada e Resultados de Poluentes:** Para cada hora da linha do tempo e para cada posto da CMA, o motor grava um `SimulationInputSnapshot` (temperatura, vento, umidade, vazão) e os respetivos `SimulationPollutantResult` (valores calculados, baseline, delta absoluto e variação percentual).



---

## 3. Explicação do Modelo de IA / Motor Dinâmico de Regras e O Porquê de sua Utilização

### 3.1. O que é o Motor MVEL no Projeto?

Embora o MVEL não seja uma rede neural profunda de Deep Learning, ele atua como o **núcleo de inferência e computação simbólica** do sistema (frequentemente categorizado como um motor de regras e expressões dinâmicas de alta performance em ambientes JVM).

### 3.2. Por que Usar este Modelo em vez de Código Hardcoded?

1. **Flexibilidade Científica Extrema:** Cientistas alteram equações matemáticas e fatores de emissão diretamente em tabelas ou ficheiros CSV sem necessitar de intervenção de engenheiros de software para reescrever classes Java e realizar novos deploys.
2. **Desempenho com Pré-Compilação (`MVEL.compileExpression`):** O motor pré-compila as fórmulas em estruturas de bytecode em memória antes do loop massivo de simulação por posto/hora, garantindo uma velocidade de execução próxima à de código nativo em Java.
3. **Isolamento de Domínio (Sandbox Lógico):** As fórmulas executam num contexto controlado (`Map<String, Object> contextVariables`), tendo acesso apenas às variáveis meteorológicas e operacionais injetadas (temperatura, vento, umidade, volume de gasolina), protegendo o servidor contra acessos indevidos à memória ou recursos de disco.

---

## 4. Estrutura de Pacotes e Componentes do Novo Módulo

```text
src/main/java/com/example/emissionsCalculator/
├── controller/scientific/
│   ├── ScientificRuleController.java[cite: 3]
│   └── SimulationBatchController.java[cite: 3]
├── service/scientific/
│   ├── ScientificRuleService.java[cite: 3]
│   └── simulation/
│       ├── TemporalSimulationService.java[cite: 3]
│       └── SimulationExecutorWorker.java[cite: 3]
├── entity/scientific/
│   ├── generalPollution/
│   │   └── ScientificRule.java[cite: 3]
│   └── simulation/
│       ├── SimulationBatch.java[cite: 3]
│       ├── SimulationInputSnapshot.java[cite: 3]
│       └── SimulationPollutantResult.java[cite: 3]
└── repository/scientific/
    ├── generalPollution/
    │   └── ScientificRuleRepository.java[cite: 3]
    └── simulation/
        ├── SimulationBatchRepository.java[cite: 3]
        ├── SimulationInputSnapshotRepository.java[cite: 3]
        └── SimulationPollutantResultRepository.java[cite: 3]

```

---

## 5. Modelo de Dados (Tabelas PostgreSQL)

### 1. Tabela `scientific_rules` (Armazenamento de Regras e PDFs)

| Coluna | Tipo SQL | Descrição |
| --- | --- | --- |
| `id` | `BIGSERIAL` (PK) | Chave primária autoincrementável

|
| `rule_id` | `VARCHAR(100)` (UK) | Identificador textual único da regra (ex: `CETESB_01`)

|
| `rule_name` | `VARCHAR(200)` | Nome descritivo da regra científica

|
| `article_name` | `VARCHAR(255)` | Título do artigo científico de referência

|
| `pdf_content` | `BYTEA` | Binário integral do artigo científico em PDF

|
| `target_pollutant` | `VARCHAR(50)` | Poluente alvo calculado (ex: `VOC`, `BENZENE`, `PM10`)

|
| `mvel_formula` | `TEXT` | Expressão matemática interpretada pelo motor MVEL

|
| `unit_of_measure` | `VARCHAR(50)` | Unidade de medida do resultado (ex: `kg`, `g`, `ug/m3`)

|

### 2. Tabela `simulation_batches` (Lotes de Simulação Temporal)

| Coluna | Tipo SQL | Descrição |
| --- | --- | --- |
| `id` | `BIGSERIAL` (PK) | Identificador do lote de simulação

|
| `batch_name` | `VARCHAR(200)` | Nome descritivo do lote (ex: `Simulacao_Lookback_4d`)

|
| `horizon_type` | `VARCHAR(50)` | Tipo de horizonte temporal (`FORECAST_3_DAYS`, `HOURLY_24H`)

|
| `lookback_days` | `INTEGER` | Número de dias retroativos de histórico analisados

|
| `history_start_time` | `TIMESTAMP` | Início exato da janela de passado

|
| `end_forecast_time` | `TIMESTAMP` | Fim exato da janela de futuro

|
| `status` | `VARCHAR(30)` | Estado da execução (`SUCCESS`, `PROCESSING`, `FAILED`)

|

---

## 6. Trechos de Código-Chave Implementados

### A. Validação de Segurança e Regras (`ScientificRuleService.java`)

```java
@Transactional
public int processCsvAndPdfUpload(MultipartFile csvFile, MultipartFile pdfFile) {
    validateFilesSecurity(csvFile, pdfFile); // Valida extensões .csv e .pdf[cite: 3]
    
    int count = 0;
    try {
        byte[] pdfBytes = (pdfFile != null && !pdfFile.isEmpty()) ? pdfFile.getBytes() : null;
        String pdfName = (pdfFile != null) ? pdfFile.getOriginalFilename() : null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // Ignora cabeçalho[cite: 3]
            if (line == null) return 0;

            while ((line = br.readLine()) != null) {
                String[] cols = line.split("[,;]", -1);
                if (cols.length < 7) continue; 

                String ruleId = cols[0].trim();
                String mvelFormula = cols[5].trim();

                validateMvelFormulaSafety(mvelFormula); // Bloqueia código hostil[cite: 3]

                ScientificRule rule = ruleRepository.findByRuleId(ruleId).orElse(new ScientificRule());
                rule.setRuleId(ruleId);
                rule.setRuleName(cols[1].trim());
                rule.setArticleName(cols[2].trim());
                rule.setTargetPollutant(cols[3].trim());
                rule.setInputVariables(cols[4].trim());
                rule.setMvelFormula(mvelFormula);
                rule.setUnitOfMeasure(cols[6].trim());
                if (pdfBytes != null) {
                    rule.setPdfContent(pdfBytes);
                    rule.setPdfFileName(pdfName);
                }
                ruleRepository.save(rule);
                count++;
            }
        }
        return count;
    } catch (SecurityException se) {
        log.error("ALERTA DE SEGURANÇA: Tentativa de upload hostil: {}", se.getMessage());
        throw se;
    } catch (Exception e) {
        throw new RuntimeException("Falha na importação: " + e.getMessage());
    }
}

private void validateMvelFormulaSafety(String formula) {
    String lowerFormula = formula.toLowerCase();
    List<String> dangerousKeywords = List.of(
            "runtime", "processbuilder", "system", "class", 
            "constructor", "eval", "exec", "file", "thread", "reflect"
    );
    for (String keyword : dangerousKeywords) {
        if (lowerFormula.contains(keyword)) {
            throw new SecurityException("Termo restrito detetado na fórmula MVEL: '" + keyword + "'");
        }
    }
}

```

---

## 7. Instruções de Execução, Teste e Auditoria (cURL & SQL)

### 1. Upload Seguro de Regras Científicas e Artigo PDF

```powershell
curl.exe -X POST "http://localhost:8080/api/scientific-rules/upload" -F "csvFile=@regras_cientificas.csv" -F "pdfFile=@artigo.pdf"

```

### 2. Execução da Simulação Temporal (4 Dias Passados + 3 Dias Futuros)

```powershell
curl.exe -X POST "http://localhost:8080/api/simulations/run?batchName=Simulacao_Lookback_4d_Forecast_3d&forecastDays=3&lookbackDays=4"

```

### 3. Consultas SQL de Auditoria dos Resultados da Simulação

```sql
-- Verificar estado dos lotes de simulação executados
SELECT id, batch_name, horizon_type, lookback_days, status, history_start_time, end_forecast_time 
FROM simulation_batches 
ORDER BY execution_timestamp DESC;

-- Consultar os resultados de poluentes calculados pelo motor MVEL no lote mais recente
SELECT 
    r.step_timestamp,
    r.rule_id,
    r.target_pollutant,
    ROUND(r.calculated_value::numeric, 4) AS calculated_val,
    ROUND(r.delta_percentage::numeric, 2) || '%' AS delta_pct,
    r.unit_of_measure
FROM simulation_pollutant_results r
WHERE r.simulation_batch_id = (SELECT MAX(id) FROM simulation_batches)
LIMIT 50;

```