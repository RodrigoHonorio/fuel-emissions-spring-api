// =================================================================
// S.P.I.R.E. - Módulo de Postos de Combustível (gas-stations.js)
// =================================================================

const gasStationsLayer = L.layerGroup();
let gasStationsData = [];
let currentSelectedFuel = 'both';

// Aguarda o carregamento do DOM/Mapa para adicionar a camada e carregar os dados
document.addEventListener('DOMContentLoaded', () => {
    if (window.map && !window.map.hasLayer(gasStationsLayer)) {
        window.map.addLayer(gasStationsLayer);
    }
    loadGasStations('both');
});

// Cria o PINO AZUL com o ícone da Bomba de Combustível (⛽)
function createGasStationPin(colorHex = '#1E88E5') {
    const svgPin = `
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 36" width="30" height="42">
            <path fill="${colorHex}" stroke="#FFFFFF" stroke-width="1.8" d="M12 0C5.373 0 0 5.373 0 12c0 9 12 24 12 24s12-15 12-24c0-6.627-5.373-12-12-12z"/>
            <circle cx="12" cy="12" r="7.5" fill="#FFFFFF"/>
            <text x="12" y="15.5" font-size="9" text-anchor="middle" fill="#333333" font-weight="bold">⛽</text>
        </svg>`;

    return L.divIcon({
        className: 'custom-gas-station-pin',
        html: svgPin,
        iconSize: [30, 42],
        iconAnchor: [15, 42],
        popupAnchor: [0, -38]
    });
}

function loadGasStations(fuelType) {
    currentSelectedFuel = fuelType;
    const url = `/api/v1/gas-stations?fuelType=${fuelType}`;
    console.log(`🔍 A procurar postos na API: ${url}`);

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error(`Erro HTTP: ${response.status}`);
            return response.json();
        })
        .then(data => {
            if (!Array.isArray(data)) {
                console.warn("⚠️ Resposta da API não é um array:", data);
                return;
            }

            gasStationsData = data.map(item => ({
                name: item.name || "Posto de Combustível",
                operator: item.operator || "Independente",
                lat: parseFloat(item.latitude),
                lng: parseFloat(item.longitude),
                pumps: item.numberOfPumps || 4,
                vocEmissionKg: parseFloat(item.estimatedDailyVocKg || 0.0)
            }));

            console.log(`✅ ${gasStationsData.length} postos recebidos com sucesso.`);
            renderGasStationsOnMap();
        })
        .catch(err => console.error("❌ Erro ao procurar postos:", err));
}

function renderGasStationsOnMap() {
    gasStationsLayer.clearLayers();

    let fuelLabel = "Ambos (Média)";
    if (currentSelectedFuel === 'petrol' || currentSelectedFuel === 'gasoline') fuelLabel = "Gasolina";
    if (currentSelectedFuel === 'diesel') fuelLabel = "Diesel";

    let countRendered = 0;

    gasStationsData.forEach(station => {
        if (isNaN(station.lat) || isNaN(station.lng)) return;

        const pinIcon = createGasStationPin('#1E88E5');
        const marker = L.marker([station.lat, station.lng], { icon: pinIcon });

        const popupContent = `
            <div style="font-family: Arial, sans-serif; font-size: 13px; line-height: 1.5; min-width: 210px;">
                <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 4px;">
                    <span style="font-size: 16px;">⛽</span>
                    <b style="font-size: 14px; color: #1E88E5;">${station.name}</b>
                </div>
                <div style="color: #555; font-size: 12px; margin-bottom: 6px;">
                    <b>Operador:</b> ${station.operator} &nbsp;|&nbsp; <b>Bombas:</b> ${station.pumps}
                </div>
                <hr style="border: 0; border-top: 1px solid #eee; margin: 6px 0;">
                <div style="font-weight: bold; color: #333; margin-bottom: 4px; font-size: 12px;">📊 Estimativa de Emissão (S.P.I.R.E.):</div>
                <div style="color: #444; font-size: 12px;">
                    • <b>COV (Voláteis):</b> <span style="color: #dc3545; font-weight: bold;">${station.vocEmissionKg.toFixed(2)} kg/dia</span><br>
                    • <b>Combustível Base:</b> ${fuelLabel}
                </div>
            </div>
        `;

        marker.bindPopup(popupContent);
        gasStationsLayer.addLayer(marker);
        countRendered++;
    });

    console.log(`📍 ${countRendered} pinos de postos ⛽ desenhados no mapa.`);
}

// Alterna a exibição dos postos de combustível no mapa de forma segura
window.toggleGasStationsLayer = function(show) {
    if (!window.map) return;
    const shouldShow = (show === true || show === 'stations' || show === 'both');
    if (shouldShow) {
        if (!window.map.hasLayer(gasStationsLayer)) {
            window.map.addLayer(gasStationsLayer);
        }
    } else {
        if (window.map.hasLayer(gasStationsLayer)) {
            window.map.removeLayer(gasStationsLayer);
        }
    }
};

window.reloadStationsWithCurrentFuel = function(fuelType) {
    loadGasStations(fuelType);
};