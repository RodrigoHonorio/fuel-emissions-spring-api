// =================================================================
// S.P.I.R.E. - Módulo de Estações Ambientais (stations.js)
// =================================================================

const LONDON_BOUNDS = { minLat: 51.28, maxLat: 51.69, minLng: -0.51, maxLng: 0.33 };

const stationsClusterGroup = L.markerClusterGroup({
    chunkedLoading: true,
    maxClusterRadius: 40,
    spiderfyOnMaxZoom: true,
    showCoverageOnHover: false
}).addTo(window.map);

loadSpatialNodes();

function createPinIcon(colorHex) {
    return L.divIcon({
        className: 'custom-pin-icon',
        html: `
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 36" width="28" height="40">
                <path fill="${colorHex}" stroke="#FFFFFF" stroke-width="1.5" d="M12 0C5.373 0 0 5.373 0 12c0 9 12 24 12 24s12-15 12-24c0-6.627-5.373-12-12-12z"/>
                <circle cx="12" cy="12" r="5" fill="#FFFFFF"/>
            </svg>`,
        iconSize: [28, 40],
        iconAnchor: [14, 40],
        popupAnchor: [0, -36]
    });
}

async function loadSpatialNodes() {
    let nodes = [];
    try {
        let response = await fetch('/api/v1/spatial-nodes?size=1000');
        if (!response.ok) response = await fetch('/spatial-nodes?size=1000');

        if (response.ok) {
            const data = await response.json();
            nodes = Array.isArray(data) ? data : (data.content || []);
        }
    } catch (error) {
        console.error("❌ Erro ao carregar estações:", error);
    }
    if (nodes.length > 0) renderStationsOnMap(nodes);
}

function renderStationsOnMap(nodes) {
    stationsClusterGroup.clearLayers();
    const markersToAdd = [];

    nodes.forEach(node => {
        const lat = parseFloat(node.latitude ?? node.lat);
        const lng = parseFloat(node.longitude ?? node.lng);
        const stationName = node.stationName ?? node.name ?? "Estação de Monitoramento";
        const temp = node.ambientTemperatureCelsius ?? 17.2;
        const windSpd = node.windSpeed ?? 3.2;
        const windDir = node.windDirection ?? 180;
        const aqi = String(node.aqiStatus ?? "LOW").toUpperCase();

        if (isNaN(lat) || isNaN(lng) || lat < LONDON_BOUNDS.minLat || lat > LONDON_BOUNDS.maxLat || lng < LONDON_BOUNDS.minLng || lng > LONDON_BOUNDS.maxLng) return;

        let pinColor = '#198754';
        let textColor = '#ffffff';

        if (aqi === 'HIGH' || aqi === 'POOR') {
            pinColor = '#dc3545';
        } else if (aqi === 'MODERATE') {
            pinColor = '#ffc107';
            textColor = '#000000';
        }

        const marker = L.marker([lat, lng], { icon: createPinIcon(pinColor) });

        const popupContent = `
            <div style="font-family: Arial, sans-serif; font-size: 13px; line-height: 1.5; min-width: 210px;">
                <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 4px;">
                    <span style="font-size: 15px;">🍃</span>
                    <b style="font-size: 14px; color: #198754;">${stationName}</b>
                </div>
                <hr style="border: 0; border-top: 1px solid #eee; margin: 6px 0;">
                <div style="margin-bottom: 6px;">
                    <b>Qualidade do Ar:</b>
                    <span style="background-color: ${pinColor}; color: ${textColor}; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: bold;">
                        ${aqi}
                    </span>
                </div>
                <div style="color: #444; font-size: 12px; line-height: 1.6;">
                    🌡️ <b>Temperatura:</b> ${temp} °C<br>
                    💨 <b>Vento:</b> ${windSpd} m/s (Direção: ${windDir}°)
                </div>
            </div>
        `;

        marker.bindPopup(popupContent);
        markersToAdd.push(marker);
    });

    stationsClusterGroup.addLayers(markersToAdd);
}

window.toggleEnvironmentLayer = function(layerType) {
    if (layerType === 'environment' || layerType === 'both') {
        if (!window.map.hasLayer(stationsClusterGroup)) window.map.addLayer(stationsClusterGroup);
    } else {
        if (window.map.hasLayer(stationsClusterGroup)) window.map.removeLayer(stationsClusterGroup);
    }
};