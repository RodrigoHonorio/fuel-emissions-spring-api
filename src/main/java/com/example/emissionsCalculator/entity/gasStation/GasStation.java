package com.example.emissionsCalculator.entity.gasStation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * <b>O que é:</b> Entidade JPA para dados espaciais de postos do OpenStreetMap[cite: 19].
 * <b>Para que serve:</b> Mapear a tabela 'gas_stations' na base de dados PostgreSQL com localização, infraestrutura e comodidades obtidas do OSM[cite: 19, 26].
 * <b>De onde veio:</b> API Overpass do OpenStreetMap[cite: 17, 24].
 */
@Entity
@Table(name = "gas_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GasStation {

    /**
     * <b>O que é:</b> Identificador único do elemento[cite: 19].
     * <b>Para que serve:</b> Chave primária da tabela, mapeando diretamente o ID original do OSM[cite: 19, 24].
     * <b>De onde veio:</b> 'id' retornado pela Overpass API[cite: 17, 24].
     */
    @Id
    private Long id;

    /** Nome do posto[cite: 19]. */
    private String name;

    /** Marca comercial[cite: 19]. */
    private String brand;

    /** Operador do posto[cite: 19]. */
    private String operator;

    /** Tag de comodidade ('fuel')[cite: 19]. */
    private String amenity;

    // Coordenadas
    /** Latitude geográfica exata[cite: 19]. */
    private Double latitude;

    /** Longitude geográfica exata[cite: 19]. */
    private Double longitude;

    // Endereço
    /** Nome da rua[cite: 19]. */
    private String street;

    /** Código postal (Postcode)[cite: 19]. */
    private String postcode;

    /** Cidade (padronizada para 'London')[cite: 19, 24]. */
    private String city;

    // Horários e Contactos
    /** Horário de funcionamento formatado[cite: 19]. */
    private String openingHours;

    /** Número de telefone[cite: 19]. */
    private String phone;

    /** Website do posto[cite: 19]. */
    private String website;

    // Tipos de Combustível Disponíveis
    private Boolean hasDiesel;
    private Boolean hasGasoline95;
    private Boolean hasGasoline98;
    private Boolean hasLpg;
    private Boolean hasElectric;

    // Serviços Adicionais
    private Boolean hasCarWash;
    private Boolean hasCompressedAir;
    private Boolean hasShop;
}