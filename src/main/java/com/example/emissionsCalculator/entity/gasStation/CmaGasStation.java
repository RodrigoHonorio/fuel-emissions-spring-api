package com.example.emissionsCalculator.entity.gasStation;

import jakarta.persistence.*;
import lombok.Data;

/**
 * <b>O que é:</b> Entidade JPA de persistência dos postos e preços da CMA (Competition and Markets Authority)[cite: 18].
 * <b>Para que serve:</b> Mapear a tabela 'cma_gas_stations' na base de dados PostgreSQL para armazenar relatórios oficiais de preços e horários[cite: 18, 26].
 * <b>De onde veio:</b> Relatório governamental em CSV da CMA UK ('UpdatedFuelPrice-1791298800051.csv')[cite: 22].
 */
@Data
@Entity
@Table(name = "cma_gas_stations")
public class CmaGasStation {

    /** Chave primária gerada automaticamente pelo banco de dados[cite: 18]. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Timestamp da última atualização do posto no relatório[cite: 18]. */
    private String forecourtUpdateTimestamp;

    /** Identificador único do nó do posto no sistema governamental (único e obrigatório)[cite: 18]. */
    @Column(unique = true, nullable = false)
    private String nodeId;

    /** Nome comercial / fantasia do posto[cite: 18]. */
    private String tradingName;

    /** Nome da marca petrolífera[cite: 18]. */
    private String brandName;

    /** Indicador se o posto fica numa autoestrada (Motorway)[cite: 18]. */
    private Boolean isMotorwayServiceStation;

    /** Indicador se o posto pertence a um supermercado[cite: 18]. */
    private Boolean isSupermarketServiceStation;

    /** Telefone público do estabelecimento[cite: 18]. */
    private String publicPhoneNumber;

    /** Indicador de fecho temporário[cite: 18]. */
    private Boolean temporaryClosure;

    /** Indicador de fecho permanente[cite: 18]. */
    private Boolean permanentClosure;

    /** Data de encerramento definitivo (se aplicável)[cite: 18]. */
    private String permanentClosureDate;

    // Localização
    /** Código postal britânico (Postcode)[cite: 18]. */
    private String postcode;

    /** Primeira linha do endereço físico[cite: 18]. */
    @Column(length = 500)
    private String addressLine1;

    /** Segunda linha do endereço físico[cite: 18]. */
    private String addressLine2;

    /** Cidade[cite: 18]. */
    private String city;

    /** Condado / Estado[cite: 18]. */
    private String county;

    /** País[cite: 18]. */
    private String country;

    /** Coordenada geográfica de Latitude[cite: 18]. */
    private Double latitude;

    /** Coordenada geográfica de Longitude[cite: 18]. */
    private Double longitude;

    // Preços e Timestamps de Combustíveis
    /** Preço de gasolina E5 (em pence)[cite: 18]. */
    private Double fuelPriceE5;
    private String priceSubmissionTimestampE5;
    private String priceChangeEffectiveTimestampE5;

    /** Preço de gasolina E10 (em pence)[cite: 18]. */
    private Double fuelPriceE10;
    private String priceSubmissionTimestampE10;
    private String priceChangeEffectiveTimestampE10;

    /** Preço de Diesel B7 Standard (em pence)[cite: 18]. */
    private Double fuelPriceB7S;
    private String priceSubmissionTimestampB7S;
    private String priceChangeEffectiveTimestampB7S;

    /** Preço de Diesel B7 Premium (em pence)[cite: 18]. */
    private Double fuelPriceB7P;
    private String priceSubmissionTimestampB7P;
    private String priceChangeEffectiveTimestampB7P;

    /** Preço de Biodiesel B10 (em pence)[cite: 18]. */
    private Double fuelPriceB10;
    private String priceSubmissionTimestampB10;
    private String priceChangeEffectiveTimestampB10;

    /** Preço de Óleo Vegetal Hidrotratado HVO (em pence)[cite: 18]. */
    private Double fuelPriceHVO;
    private String priceSubmissionTimestampHVO;
    private String priceChangeEffectiveTimestampHVO;

    // Horários de Funcionamento
    private String mondayOpenTime;
    private String mondayCloseTime;
    private Boolean mondayIs24Hours;

    private String tuesdayOpenTime;
    private String tuesdayCloseTime;
    private Boolean tuesdayIs24Hours;

    private String wednesdayOpenTime;
    private String wednesdayCloseTime;
    private Boolean wednesdayIs24Hours;

    private String thursdayOpenTime;
    private String thursdayCloseTime;
    private Boolean thursdayIs24Hours;

    private String fridayOpenTime;
    private String fridayCloseTime;
    private Boolean fridayIs24Hours;

    private String saturdayOpenTime;
    private String saturdayCloseTime;
    private Boolean saturdayIs24Hours;

    private String sundayOpenTime;
    private String sundayCloseTime;
    private Boolean sundayIs24Hours;

    private String bankHolidayOpenTime;
    private String bankHolidayCloseTime;
    private Boolean bankHolidayIs24Hours;

    // Comodidades (Amenities)
    private Boolean adbluePumps;
    private Boolean adbluePackaged;
    private Boolean lpgPumps;
    private Boolean carWash;
    private Boolean airPumpOrScreenwash;
    private Boolean waterFilling;
    private Boolean twentyFourHourFuel;
    private Boolean customerToilets;
}