package com.example.emissionsCalculator.service.boundary.enums;

import lombok.Getter;

/**
 * <b>O que é:</b> Enum que armazena as camadas de mapas de fundo (tile layers) gratuitas.
 * <b>Para que serve:</b> Fornecer metadados visuais, URLs de servidores de mapa e termos de atribuição para o frontend de forma desacoplada.
 */
@Getter
public enum MapLayer {

    OSM("osm", "OpenStreetMap (Padrão)", "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", "&copy; OpenStreetMap contributors", 19),
    TOPO("topo", "OpenTopoMap (Topográfico/Relevo)", "https://{s}.opentopomap.org/{z}/{x}/{y}.png", "Map data: &copy; OpenStreetMap contributors, SRTM | Map style: &copy; OpenTopoMap (CC-BY-SA)", 17),
    WIKIMEDIA("wikimedia", "Wikimedia Maps", "https://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png", "&copy; OpenStreetMap contributors & Wikimedia", 19),
    SATELLITE("satellite", "Esri World Imagery (Satélite)", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", "Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community", 19),
    CYCLOSM("cyclosm", "CyclOSM (Ciclovias/Urbano)", "https://{s}.tile-cyclosm.openstreetmap.org/cyclosm/{z}/{x}/{y}.png", "&copy; OpenStreetMap contributors &mdash; Style: CyclOSM (CC-BY-SA)", 19);

    private final String id;
    private final String name;
    private final String urlTemplate;
    private final String attribution;
    private final int maxZoom;

    MapLayer(String id, String name, String urlTemplate, String attribution, int maxZoom) {
        this.id = id;
        this.name = name;
        this.urlTemplate = urlTemplate;
        this.attribution = attribution;
        this.maxZoom = maxZoom;
    }
}