package uk.org.spire.emissionsCalculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteDto {

    @JsonProperty("@SiteName")
    private String siteName;

    @JsonProperty("@Latitude")
    private Double latitude;

    @JsonProperty("@Longitude")
    private Double longitude;

    // Getters e Setters
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}