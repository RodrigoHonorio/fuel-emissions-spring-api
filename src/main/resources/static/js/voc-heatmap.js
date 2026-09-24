/**
 * Módulo de Renderização do Heatmap (Manchas de Poluição de COV)
 */
let vocHeatmapLayer = null;

function fetchAndRenderVocData(params) {
    const query = new URLSearchParams({
        temp: params.temp,
        windSpeed: params.windSpeed,
        windDirection: params.windDirection,
        fuelType: params.fuelType,
        humidity: params.humidity || 60.0,
        pressure: 1013.0,
        solarRad: 400.0
    }).toString();

    fetch(`/api/v1/emissions/voc-heatmap?${query}`)
        .then(response => {
            if (!response.ok) throw new Error('Falha na resposta da API.');
            return response.json();
        })
        .then(data => {
            // Renderiza APENAS as manchas de poluição
            renderHeatmap(data.heatmapPoints);
        })
        .catch(error => {
            console.error('❌ Erro ao carregar os dados de COV:', error);
        });
}

function renderHeatmap(points) {
    if (vocHeatmapLayer) {
        window.map.removeLayer(vocHeatmapLayer);
    }

    const toggle = document.getElementById('toggleHeatmap');
    if (!toggle || !toggle.checked || !points || points.length === 0) {
        return;
    }

    if (typeof L.heatLayer !== 'function') return;

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

    vocHeatmapLayer.addTo(window.map);
}