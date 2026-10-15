package com.example.emissionsCalculator.repository.spatial;

import com.example.emissionsCalculator.entity.spatial.SpatialHotspot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpatialHotspotRepository extends JpaRepository<SpatialHotspot, Long> {
    List<SpatialHotspot> findBySeverityLevel(String severityLevel);
    List<SpatialHotspot> findByBoroughCode(String boroughCode);
}