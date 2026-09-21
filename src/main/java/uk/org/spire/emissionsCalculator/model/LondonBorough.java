package uk.org.spire.emissionsCalculator.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "london_boroughs")
public class LondonBorough {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "borough_name", unique = true, nullable = false)
    private String name;

    @Column(name = "borough_code")
    private String code;

    @Column(name = "region")
    private String region;

    // Corrigido: Removido 'columnifiers' e mantido apenas columnDefinition
    @Column(name = "geom", columnDefinition = "Geometry")
    private Geometry geometry;

    public LondonBorough() {}

    public LondonBorough(String name, String code, String region, Geometry geometry) {
        this.name = name;
        this.code = code;
        this.region = region;
        this.geometry = geometry;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Geometry getGeometry() { return geometry; }
    public void setGeometry(Geometry geometry) { this.geometry = geometry; }
}