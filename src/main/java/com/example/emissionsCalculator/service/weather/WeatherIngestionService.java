package com.example.emissionsCalculator.service.weather;

import com.example.emissionsCalculator.dto.weather.OpenMeteoResponseDTO;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>O que é:</b> Serviço de ingestão e sincronização de dados meteorológicos.
 * <b>Para que serve:</b> Consultar a Open-Meteo API para os próximos 16 dias (384 horas) e salvar/atualizar os registos na base de dados.
 * <b>De onde veio:</b> Chamada à Open-Meteo API pública via RestClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherIngestionService {

    private final HourlyWeatherRecordRepository repository;
    private final RestClient restClient = RestClient.create();

    /** Coordenadas centrais da Grande Londres. */
    private static final String LATITUDE = "51.5074";
    private static final String LONGITUDE = "-0.1278";
    private static final String LOCATION_NAME = "Greater London";

    /** Endpoint da Open-Meteo configurado para 16 dias de previsão hora a hora. */
    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=" + LATITUDE +
            "&longitude=" + LONGITUDE +
            "&hourly=temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,wind_speed_10m" +
            "&forecast_days=16";

    /**
     * <b>O que é:</b> Método principal de sincronização meteorológica.
     * <b>Para que serve:</b> Fazer o download das 384 horas de dados e atualizar/inserir no PostgreSQL.
     * @return Quantidade de registos horários processados com sucesso.
     */
    @Transactional
    public int syncWeatherForecast() {
        log.info("⏳ A obter dados de previsão meteorológica da Open-Meteo para {}...", LOCATION_NAME);

        try {
            OpenMeteoResponseDTO response = restClient.get()
                    .uri(OPEN_METEO_URL)
                    .retrieve()
                    .body(OpenMeteoResponseDTO.class);

            if (response == null || response.hourly() == null || response.hourly().time() == null) {
                log.warn("⚠️ Resposta nula ou vazia recebida da Open-Meteo API.");
                return 0;
            }

            var hourly = response.hourly();
            List<String> times = hourly.time();
            List<HourlyWeatherRecord> recordsToSave = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

            for (int i = 0; i < times.size(); i++) {
                LocalDateTime forecastTimestamp = LocalDateTime.parse(times.get(i), formatter);

                // Upsert: Procura se a hora já existe; se não existir, cria uma nova
                HourlyWeatherRecord record = repository
                        .findByLocationNameAndForecastTimestamp(LOCATION_NAME, forecastTimestamp)
                        .orElse(HourlyWeatherRecord.builder()
                                .locationName(LOCATION_NAME)
                                .forecastTimestamp(forecastTimestamp)
                                .build());

                record.setTemperatureCelsius(getSafeValue(hourly.temperature2m(), i));
                record.setApparentTemperatureCelsius(getSafeValue(hourly.apparentTemperature(), i));
                record.setRelativeHumidityPercentage(getSafeValue(hourly.relativeHumidity2m(), i));
                record.setPrecipitationMm(getSafeValue(hourly.precipitation(), i));
                record.setWindSpeedKmh(getSafeValue(hourly.windSpeed10m(), i));
                record.setUpdatedAt(LocalDateTime.now());

                recordsToSave.add(record);
            }

            repository.saveAll(recordsToSave);
            log.info("✅ Sincronização meteorológica concluída! {} horas de dados de clima atualizados no banco.", recordsToSave.size());
            return recordsToSave.size();

        } catch (Exception e) {
            log.error("❌ Erro ao sincronizar dados meteorológicos: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na sincronização meteorológica: " + e.getMessage());
        }
    }

    private Double getSafeValue(List<Double> list, int index) {
        if (list != null && index < list.size()) {
            return list.get(index);
        }
        return null;
    }
}
