package uk.org.spire.emissionsCalculator.dto;

import java.util.List;

public class VocHeatmapDTO {
    private double windSpeed;
    private double windDirection;
    private List<HeatmapPointDTO> heatmapPoints;
    private List<PlumeVectorDTO> plumeVectors;

    public VocHeatmapDTO(double windSpeed, double windDirection, List<HeatmapPointDTO> heatmapPoints, List<PlumeVectorDTO> plumeVectors) {
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.heatmapPoints = heatmapPoints;
        this.plumeVectors = plumeVectors;
    }

    public double getWindSpeed() { return windSpeed; }
    public double getWindDirection() { return windDirection; }
    public List<HeatmapPointDTO> getHeatmapPoints() { return heatmapPoints; }
    public List<PlumeVectorDTO> getPlumeVectors() { return plumeVectors; }

    public static class HeatmapPointDTO {
        private double lat;
        private double lng;
        private double intensity; // Concentração total acumulada em µg/m³

        public HeatmapPointDTO(double lat, double lng, double intensity) {
            this.lat = lat;
            this.lng = lng;
            this.intensity = intensity;
        }

        public double getLat() { return lat; }
        public double getLng() { return lng; }
        public double getIntensity() { return intensity; }
    }

    public static class PlumeVectorDTO {
        private String stationName;
        private double originLat;
        private double originLng;
        private double endLat;
        private double endLng;
        private double vocKgPerDay;

        public PlumeVectorDTO(String stationName, double originLat, double originLng, double endLat, double endLng, double vocKgPerDay) {
            this.stationName = stationName;
            this.originLat = originLat;
            this.originLng = originLng;
            this.endLat = endLat;
            this.endLng = endLng;
            this.vocKgPerDay = vocKgPerDay;
        }

        public String getStationName() { return stationName; }
        public double getOriginLat() { return originLat; }
        public double getOriginLng() { return originLng; }
        public double getEndLat() { return endLat; }
        public double getEndLng() { return endLng; }
        public double getVocKgPerDay() { return vocKgPerDay; }
    }
}