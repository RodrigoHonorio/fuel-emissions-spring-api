/**
 * Módulo de Renderização do Heatmap Somado e Vetores de Pluma de COV
 */
let vocHeatmapLayer = null;
let plumeGroup = L.layerGroup();

document.addEventListener('DOMContentLoaded', () => {
    if (typeof map !== 'undefined') {
        plumeGroup.addTo(map);
    }
});

function fetchAndRenderVocData(params) {
    console.log("🔄 A solicitar dados de COV com os parâmetros:", params);

    const query = new URLSearchParams({
        temp: params.temp,
        windSpeed: params.windSpeed,
        windDirection: params.windDirection,
        fuelType: params.fuelType,
        humidity: 60.0,
        pressure: 1013.0,
        solarRad: 400.0
    }).toString();

    fetch(`/api/v1/emissions/voc-heatmap?${query}`)
        .then(response => {
            if (!response.ok) throw new Error('Falha na resposta da API.');
            return response.json();
        })
        .then(data => {
            console.log("✅ Dados recebidos do backend:", data);
            renderHeatmap(data.heatmapPoints);
            renderPlumes(data.plumeVectors, data.windDirection);
        })
        .catch(error => {
            console.error('❌ Erro ao carregar os dados de COV:', error);
        });
}

function renderHeatmap(points) {
    if (vocHeatmapLayer) {
        map.removeLayer(vocHeatmapLayer);
    }

    const isVisible = document.getElementById('toggleHeatmap').checked;
    if (!isVisible || !points || points.length === 0) {
        console.warn("⚠️ Nenhum ponto de heatmap para renderizar ou camada desativada.");
        return;
    }

    if (typeof L.heatLayer !== 'function') {
        console.error("❌ Erro crítico: L.heatLayer não está disponível. O script leaflet-heat.js precisa de carregar antes.");
        return;
    }

    const heatData = points.map(p => [p.lat, p.lng, p.intensity]);

    vocHeatmapLayer = L.heatLayer(heatData, {
        radius: 25,
        blur: 15,
        maxZoom: 14,
        max: 50.0,
        gradient: {
            0.1: 'blue',
            0.3: 'cyan',
            0.5: 'lime',
            0.7: 'yellow',
            1.0: 'red'
        }
    });

    vocHeatmapLayer.addTo(map);
}

function renderPlumes(plumes, windDirection) {
    plumeGroup.clearLayers();

    const isVisible = document.getElementById('togglePlumes').checked;
    if (!isVisible || !plumes || plumes.length === 0) return;

    plumes.forEach(plume => {
        const line = L.polyline(
            [[plume.originLat, plume.originLng], [plume.endLat, plume.endLng]],
            {
                color: '#e74c3c',
                weight: 2,
                opacity: 0.85,
                dashArray: '6, 6'
            }
        );

        line.bindPopup(`
            <div style="font-size:12px;">
                <b>📍 Posto:</b> ${plume.stationName}<br>
                <b>💨 Taxa de Emissão:</b> ${plume.vocKgPerDay} kg/dia<br>
                <b>🧭 Direção do Vento:</b> ${windDirection}°
            </div>
        `);

        plumeGroup.addLayer(line);
    });
}