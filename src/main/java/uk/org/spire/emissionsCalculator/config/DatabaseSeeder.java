package uk.org.spire.emissionsCalculator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.org.spire.emissionsCalculator.constant.FuelType;
import uk.org.spire.emissionsCalculator.repository.EmissionRepository;
import uk.org.spire.emissionsCalculator.service.EmissionCalculationService;

/**
 * Classe responsável por popular o banco de dados do S.P.I.R.E.
 * automaticamente com as estações de Londres na primeira vez que a aplicação sobe.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final EmissionCalculationService calculationService;
    private final EmissionRepository emissionRepository;

    @Autowired
    public DatabaseSeeder(EmissionCalculationService calculationService, EmissionRepository emissionRepository) {
        this.calculationService = calculationService;
        this.emissionRepository = emissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica se o banco está vazio. Se estiver, popula com os dados de Londres.
        if (emissionRepository.count() == 0) {
            System.out.println("🌱 S.P.I.R.E.: Banco de dados vazio. Populando PostGIS com dezenas de estações em Londres...");

            // (Tipo de Combustível, Volume Litros, Temp °C, Vento km/h, Direção Vento, Latitude, Longitude)

            // 1. Westminster
            calculationService.calculateAndSaveEmissions(FuelType.PETROL, 1500.0, 20.0, 3.5, 180.0, 51.5074, -0.1278);

            // 2. Camden
            calculationService.calculateAndSaveEmissions(FuelType.DIESEL, 1800.0, 22.0, 4.0, 190.0, 51.5390, -0.1426);

            // 3. Greenwich
            calculationService.calculateAndSaveEmissions(FuelType.PETROL, 2100.0, 19.5, 3.0, 170.0, 51.4826, 0.0000);

            // 4. Islington
            calculationService.calculateAndSaveEmissions(FuelType.ETHANOL, 1200.0, 21.0, 3.2, 160.0, 51.5362, -0.1030);

            // 5. Kensington
            calculationService.calculateAndSaveEmissions(FuelType.PETROL, 2500.0, 23.0, 2.5, 150.0, 51.4988, -0.1947);

            // 6. Hackney
            calculationService.calculateAndSaveEmissions(FuelType.DIESEL, 1650.0, 20.5, 4.1, 200.0, 51.5450, -0.0553);

            // 7. Tower Hamlets
            calculationService.calculateAndSaveEmissions(FuelType.PETROL, 1900.0, 22.5, 3.8, 180.0, 51.5099, -0.0352);

            // 8. Southwark
            calculationService.calculateAndSaveEmissions(FuelType.DIESEL, 2200.0, 21.5, 3.4, 210.0, 51.5035, -0.0982);

            // 9. Wandsworth
            calculationService.calculateAndSaveEmissions(FuelType.ETHANOL, 1400.0, 19.0, 3.9, 175.0, 51.4567, -0.1910);

            // 10. Brent - Harlesden (o da sua foto!)
            calculationService.calculateAndSaveEmissions(FuelType.PETROL, 1750.0, 20.0, 3.1, 165.0, 51.5371, -0.2493);

            // 11. Lambeth / Brixton
            calculationService.calculateAndSaveEmissions(FuelType.DIESEL, 1300.0, 18.5, 4.2, 220.0, 51.4613, -0.1246);

            // 12. Hammersmith
            calculationService.calculateAndSaveEmissions(FuelType.PETROL, 2000.0, 24.0, 2.8, 140.0, 51.5152, -0.2060);

            // 13. Stratford / Newham
            calculationService.calculateAndSaveEmissions(FuelType.ETHANOL, 1600.0, 21.0, 3.3, 195.0, 51.5448, -0.0150);

            // 14. Clapham
            calculationService.calculateAndSaveEmissions(FuelType.PETROL, 1450.0, 20.0, 3.6, 185.0, 51.4620, -0.1470);

            System.out.println("✅ S.P.I.R.E.: Banco de dados populado com sucesso! Os pinos estarão visíveis no mapa.");
        } else {
            System.out.println("⚡ S.P.I.R.E.: O banco PostGIS já possui estações cadastradas. Ignorando a carga inicial (Seeder).");
        }
    }
}