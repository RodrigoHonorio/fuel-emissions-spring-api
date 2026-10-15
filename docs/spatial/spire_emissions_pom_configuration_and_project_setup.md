# S.P.I.R.E. Emissions London - Project Configuration & Setup Guide

This document contains the core configuration (`pom.xml`) and architectural layout for the **S.P.I.R.E. (System for Pollution & Integrated Refueling Emissions)** platform, designed for Greater London.

---

## 1. Project Dependencies & Build Configuration (`pom.xml`)

Ensure your Maven `pom.xml` contains the necessary dependencies for Spring Boot, PostGIS spatial support (`hibernate-spatial`), and JSON processing:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>emissions-calculator</artifactId>
    <version>1.0.0</version>
    <name>emissions-calculator</name>
    <description>SPIRE Atmospheric Emissions Geoprocessing & Simulation Platform for London</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- PostgreSQL & Spatial/PostGIS Support -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-spatial</artifactId>
        </dependency>

        <!-- Lombok for Boilerplate Reduction -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>Test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>emissions-calculator</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 2. Core Packages & Architecture Summary

The backend codebase is organized into modular packages to maintain clean separation of concerns:

1. **`com.example.emissionsCalculator.controller`**: REST controllers exposing endpoints for spatial analysis (`/api/spatial`), scientific models (`/api/scientific-models`), and system synchronization (`/api/system`).
2. **`com.example.emissionsCalculator.service`**: Orchestration logic including `MasterDataSyncService`, `SpatialHotspotService`, and historical ingestion pipelines.
3. **`com.example.emissionsCalculator.engine`**: Mathematical and scientific computation engines, including `SpatialInterpolationEngine` (IDW/Kriging), `GaussianPlumeCalculator`, and `StationEmissionsEngine` (CETESB 2015 / EPA AP-42).
4. **`com.example.emissionsCalculator.entity` & `repository`**: JPA entities mapping relational tables and PostGIS spatial geometries (`Point`, `Polygon`, `MultiPolygon`) with high-performance query repositories.

---

## 3. Quick Execution Guide

To run the complete data pipeline and populate all spatial hotspots and scientific calculations:

```bash
curl -X POST "http://localhost:8080/api/system/sync-all-json"
```

This will trigger the ingestion of Greater London administrative boundaries, CMA/OSM gas stations, LAQN air quality records, Open-Meteo weather forecasts, and compute the S.P.I.R.E. environmental models in real-time.