# Documentação Técnica e Científica: Ingestão de Dados de Preços de Combustíveis da CMA (Competition and Markets Authority)

---

## 1. Visão Geral para Pesquisadores e Analistas Econômicos (Sem Necessidade de Programação)

### 1.1. Contexto e Fundamentação Regulatória
A **CMA (Competition and Markets Authority)** é o órgão regulador de concorrência e mercados do Reino Unido. No âmbito do programa nacional de transparência do mercado varejista de combustíveis (*Road Fuel Price Transparency Scheme*), os grandes distribuidores e redes de postos reportam periodicamente seus dados estruturados de localização e preços vigentes.

### 1.2. Relevância Científica dos Dados
Diferente dos dados do OpenStreetMap (que focam na infraestrutura física/geográfica), os dados da CMA fornecem a **dimensão econômica e temporal**:
* **Monitoramento de Preços:** Variação diária/semanal dos preços de E5 (Gasolina Standard), E10 (Gasolina Premium), B7 (Diesel) e SDV (Super Diesel).
* **Análise Concorrencial:** Estudo da dispersão de preços entre diferentes marcas em regiões metropolitanas e rodovias.

### 1.3. Dicionário de Dados do Esquema
| Campo / Coluna CSV | Definição Econômica / Operacional | Unidade / Formato |
| :--- | :--- | :--- |
| `site_id` | Identificador único do estabelecimento comercial no registro do governo/CMA. | String alfanumérica |
| `brand` | Marca comercial operadora do posto (ex: BP, Shell, Asda, Sainsbury's). | Texto |
| `postcode` | Código postal exato do posto de combustível no Reino Unido. | Formato UK Postcode (ex: SW1A 1AA) |
| `e5` / `e10` / `b7` / `sdv` | Preço por litro dos respetivos combustíveis. | Valores numéricos em pence/libra estelina |
| `price_change_timestamp` | Data e hora exata da última alteração de preço praticada no posto. | ISO 8601 / Timestamp |

---

## 2. Procedimento de Obtenção do Arquivo CSV e Configuração no Projeto

Para garantir que o sistema processe dados atualizados, siga os passos de obtenção manual ou automatizada:

### 2.1. Como se Cadastrar e Obter os Arquivos
1. **Cadastro Oficial:** Acesse o portal oficial da CMA no GOV.UK (*Temporary Road Fuel Price Transparency Scheme*).
2. **Subscrição de Atualizações:** Cadastre o e-mail da equipe de pesquisa/administração no serviço de alertas ou utilize a chave API fornecida pelo agregador oficial (FuelFinder / GOV.UK Open Data).
3. **Recebimento de Arquivos:** Os relatórios atualizados são disponibilizados em links para download direto ou anexados em e-mails de distribuição no formato `.csv` ou `.json`.

### 2.2. Onde Colocar o Arquivo no Projeto
Após realizar o download do arquivo CSV atualizado:

1. Navegue até o diretório interno do projeto Spring Boot:
   ```
   src/main/resources/static.data/
   ```
2. **Padrão de Nomenclatura:** 
   O arquivo deve ser salvo mantendo o padrão esperado pelo serviço, por exemplo:
   ```
   UpdatedFuelPrice-<TIMESTAMP>.csv
   ```
   *(Exemplo atual no projeto: `UpdatedFuelPrice-1791298800051.csv`)*.

---

## 3. Visão Técnica para Engenheiros de Software e Desenvolvedores

### 3.1. Fluxo de Ingestão do Arquivo Local
O serviço lê o arquivo estático contido na pasta de recursos (*classpath/resources*), realiza o parsing linha a linha e persiste os registros na tabela `cma_gas_stations`.

```
[ Arquivo CSV em resources/static.data/ ]
         │
         ▼
[ CmaGasStationIngestionService ] ── (Parsing CSV / Apache Commons CSV / Jackson)
         │
         ▼
[ FuelFinderFeedDTO ] ────────────── (Mapeamento temporário das linhas)
         │
         ▼
[ CmaGasStation (JPA Entity) ] ───── (Conversão de Tipos e Validação)
         │
         ▼
[ Banco de Dados PostgreSQL ]
```

### 3.2. Mapeamento de Componentes de Software
1. **DTO (`FuelFinderFeedDTO.java`):** Representação do esquema do feed de preços da CMA.
2. **Entidade (`CmaGasStation.java`):** Mapeia a tabela `cma_gas_stations` no PostgreSQL.
3. **Serviço (`CmaGasStationIngestionService.java`):**
   * Carrega o recurso via `ClassPathResource("static.data/UpdatedFuelPrice-....csv")`.
   * Processa o stream de dados com tratamento de exceções para valores nulos ou vazios.
   * Salva em lote (*batch processing*) via `repository.saveAll()`.
4. **Controlador (`CmaGasStationIngestionController.java`):**
   * Expõe o endpoint `POST /api/cma-gas-stations/sync`.

---

## 4. Instruções de Execução e Teste

### Como Executar a Ingestão
* **Via Swagger UI:** Aceder a `http://localhost:8081/swagger-ui/index.html` $ightarrow$ `cma-gas-station-ingestion-controller` $ightarrow$ `POST /api/cma-gas-stations/sync` $ightarrow$ **Execute**.
* **Via cURL:**
  ```bash
  curl -X POST "http://localhost:8081/api/cma-gas-stations/sync"
  ```
