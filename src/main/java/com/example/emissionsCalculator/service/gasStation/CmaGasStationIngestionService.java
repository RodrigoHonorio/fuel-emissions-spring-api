package com.example.emissionsCalculator.service.gasStation;

import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * <b>O que é:</b> Serviço de ingestão de dados governamentais da CMA.
 * <b>Para que serve:</b> Ler e processar o ficheiro CSV local com preços e comodidades da CMA, filtrando estritamente para Londres e salvando/atualizando os registos no banco de dados.
 * <b>De onde veio:</b> Ficheiro local 'static/data/UpdatedFuelPrice-1791298800051.csv'.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CmaGasStationIngestionService {

    /** Repositório de persistência JPA. */
    private final CmaGasStationRepository repository;

    /**
     * <b>O que é:</b> Método principal de sincronização dos dados da CMA.
     * <b>Para que serve:</b> Fazer o parse linha a linha do CSV, filtrar apenas Londres e guardar os postos na tabela 'cma_gas_stations'.
     * <b>De onde veio:</b> Chamado pelo controlador 'CmaGasStationIngestionController'.
     *
     * @return Número de linhas/registos de Londres processados com sucesso.
     */
    @Transactional
    public int syncCmaGasStations() {
        int count = 0;
        try {
            ClassPathResource resource = new ClassPathResource("static/data/UpdatedFuelPrice-1791298800051.csv");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line = br.readLine(); // Ignorar o cabeçalho
                if (line == null) return 0;

                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",", -1);

                    if (cols.length < 68) continue; // Validação de integridade do número de colunas

                    String postcode = cols[10].trim();
                    String city = cols[13].trim();

                    // 🛑 FILTRO DE ESCOPO: Ignora registos que não pertençam a Londres
                    if (!isLondon(postcode, city)) {
                        continue;
                    }

                    String nodeId = cols[1].trim();

                    CmaGasStation station = repository.findByNodeId(nodeId).orElse(new CmaGasStation());

                    station.setForecourtUpdateTimestamp(cols[0].trim());
                    station.setNodeId(nodeId);
                    station.setTradingName(cols[2].trim());
                    station.setBrandName(cols[3].trim());
                    station.setIsMotorwayServiceStation(parseBoolean(cols[4]));
                    station.setIsSupermarketServiceStation(parseBoolean(cols[5]));
                    station.setPublicPhoneNumber(cols[6].trim());
                    station.setTemporaryClosure(parseBoolean(cols[7]));
                    station.setPermanentClosure(parseBoolean(cols[8]));
                    station.setPermanentClosureDate(cols[9].trim());

                    // Localização
                    station.setPostcode(postcode);
                    station.setAddressLine1(cols[11].trim());
                    station.setAddressLine2(cols[12].trim());
                    station.setCity(city);
                    station.setCounty(cols[14].trim());
                    station.setCountry(cols[15].trim());
                    station.setLatitude(parseDouble(cols[16]));
                    station.setLongitude(parseDouble(cols[17]));

                    // Preços
                    station.setFuelPriceE5(parseDouble(cols[18]));
                    station.setPriceSubmissionTimestampE5(cols[19].trim());
                    station.setPriceChangeEffectiveTimestampE5(cols[20].trim());

                    station.setFuelPriceE10(parseDouble(cols[21]));
                    station.setPriceSubmissionTimestampE10(cols[22].trim());
                    station.setPriceChangeEffectiveTimestampE10(cols[23].trim());

                    station.setFuelPriceB7S(parseDouble(cols[24]));
                    station.setPriceSubmissionTimestampB7S(cols[25].trim());
                    station.setPriceChangeEffectiveTimestampB7S(cols[26].trim());

                    station.setFuelPriceB7P(parseDouble(cols[27]));
                    station.setPriceSubmissionTimestampB7P(cols[28].trim());
                    station.setPriceChangeEffectiveTimestampB7P(cols[29].trim());

                    station.setFuelPriceB10(parseDouble(cols[30]));
                    station.setPriceSubmissionTimestampB10(cols[31].trim());
                    station.setPriceChangeEffectiveTimestampB10(cols[32].trim());

                    station.setFuelPriceHVO(parseDouble(cols[33]));
                    station.setPriceSubmissionTimestampHVO(cols[34].trim());
                    station.setPriceChangeEffectiveTimestampHVO(cols[35].trim());

                    // Horários
                    station.setMondayOpenTime(cols[36].trim());
                    station.setMondayCloseTime(cols[37].trim());
                    station.setMondayIs24Hours(parseBoolean(cols[38]));

                    station.setTuesdayOpenTime(cols[39].trim());
                    station.setTuesdayCloseTime(cols[40].trim());
                    station.setTuesdayIs24Hours(parseBoolean(cols[41]));

                    station.setWednesdayOpenTime(cols[42].trim());
                    station.setWednesdayCloseTime(cols[43].trim());
                    station.setWednesdayIs24Hours(parseBoolean(cols[44]));

                    station.setThursdayOpenTime(cols[45].trim());
                    station.setThursdayCloseTime(cols[46].trim());
                    station.setThursdayIs24Hours(parseBoolean(cols[47]));

                    station.setFridayOpenTime(cols[48].trim());
                    station.setFridayCloseTime(cols[49].trim());
                    station.setFridayIs24Hours(parseBoolean(cols[50]));

                    station.setSaturdayOpenTime(cols[51].trim());
                    station.setSaturdayCloseTime(cols[52].trim());
                    station.setSaturdayIs24Hours(parseBoolean(cols[53]));

                    station.setSundayOpenTime(cols[54].trim());
                    station.setSundayCloseTime(cols[55].trim());
                    station.setSundayIs24Hours(parseBoolean(cols[56]));

                    station.setBankHolidayOpenTime(cols[57].trim());
                    station.setBankHolidayCloseTime(cols[58].trim());
                    station.setBankHolidayIs24Hours(parseBoolean(cols[59]));

                    // Comodidades
                    station.setAdbluePumps(parseBoolean(cols[60]));
                    station.setAdbluePackaged(parseBoolean(cols[61]));
                    station.setLpgPumps(parseBoolean(cols[62]));
                    station.setCarWash(parseBoolean(cols[63]));
                    station.setAirPumpOrScreenwash(parseBoolean(cols[64]));
                    station.setWaterFilling(parseBoolean(cols[65]));
                    station.setTwentyFourHourFuel(parseBoolean(cols[66]));
                    if (cols.length > 67) {
                        station.setCustomerToilets(parseBoolean(cols[67]));
                    }

                    repository.save(station);
                    count++;
                }
            }
            log.info("✅ SUCESSO: {} registos de postos em Londres (CMA) processados e gravados com sucesso!", count);
            return count;
        } catch (Exception e) {
            log.error("❌ ERRO ao processar os dados da CMA: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na sincronização da CMA: " + e.getMessage());
        }
    }

    /**
     * Valida se o posto pertence à Grande Londres com base no prefixo do Postcode ou na cidade.
     */
    private boolean isLondon(String postcode, String city) {
        if (postcode == null) return false;
        String cleanPostcode = postcode.trim().toUpperCase();

        boolean isLondonPostcode = cleanPostcode.matches("^(EC|WC|E|N|NW|SE|SW|W)[0-9].*");
        boolean isLondonCity = city != null && city.toLowerCase().contains("london");

        return isLondonPostcode || isLondonCity;
    }

    /** Helper para conversão segura de String para Double. */
    private Double parseDouble(String val) {
        try {
            if (val == null || val.trim().isEmpty()) return null;
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Helper para conversão segura de String para Boolean. */
    private Boolean parseBoolean(String val) {
        if (val == null || val.trim().isEmpty()) return false;
        return Boolean.parseBoolean(val.trim());
    }
}