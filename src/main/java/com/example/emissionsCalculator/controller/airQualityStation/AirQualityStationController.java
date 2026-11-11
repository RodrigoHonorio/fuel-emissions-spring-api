package com.example.emissionsCalculator.controller.airQualityStation;

import com.example.emissionsCalculator.entity.airQualityStation.AirQualityStation;
import com.example.emissionsCalculator.repository.airQualityStation.AirQualityStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/air-quality-stations")
@RequiredArgsConstructor
public class AirQualityStationController {

    private final AirQualityStationRepository repository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AirQualityStation>> getAllStations() {
        List<AirQualityStation> stations = repository.findAll();
        return ResponseEntity.ok(stations);
    }
}