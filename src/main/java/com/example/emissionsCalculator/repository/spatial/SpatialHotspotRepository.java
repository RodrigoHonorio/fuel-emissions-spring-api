package com.example.emissionsCalculator.repository.spatial;

import com.example.emissionsCalculator.entity.spatial.SpatialHotspot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpatialHotspotRepository extends JpaRepository<SpatialHotspot, Long> {

    List<SpatialHotspot> findByCalculationTimestamp(LocalDateTime calculationTimestamp);

    List<SpatialHotspot> findByPollutantTypeAndCalculationTimestamp(String pollutantType, LocalDateTime calculationTimestamp);

    List<SpatialHotspot> findByBoroughCode(String boroughCode);
}
