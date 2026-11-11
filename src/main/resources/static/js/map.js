const londonCoords = [51.5074, -0.1278];
const map = L.map('map', { zoomControl: false }).setView(londonCoords, 11);

L.control.zoom({ position: 'topleft' }).addTo(map);

// Camada base padrão (OpenStreetMap)
let currentBaseLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 19
}).addTo(map);

function changeMapStyle(style) {
    map.removeLayer(currentBaseLayer);
    let url = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
    if (style === 'dark') {
        url = 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png';
    } else if (style === 'satellite') {
        url = 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}';
    }
    currentBaseLayer = L.tileLayer(url, { maxZoom: 19 }).addTo(map);
}

// Grupos de Camadas do Leaflet
const fuelGroup = L.layerGroup().addTo(map);
const airGroup = L.layerGroup().addTo(map);
const hotspotsGroup = L.layerGroup().addTo(map);
const boroughsGroup = L.layerGroup().addTo(map);
let heatmapLayer = null;

// Função Auxiliar para injetar o Token JWT em todas as requisições Fetch
function getAuthHeaders() {
    const token = localStorage.getItem('jwt_token');
    return token ? { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' };
}

// Função para definir a cor baseada na severidade
function getSeverityColor(value, type) {
    if (type === 'severity') {
        switch((value || '').toUpperCase()) {
            case 'DANGER':
            case 'CRITICAL': return '#ef4444'; // Vermelho
            case 'WARNING': return '#f59e0b';  // Amarelo / Laranja
            default: return '#22c55e';         // Verde
        }
    }
    if (value > 35.0) return '#ef4444'; // Crítico
    if (value > 15.0) return '#f59e0b'; // Alerta
    return '#22c55e';                    // Normal
}

// Ícones personalizados com cores dinâmicas e FontAwesome
function createCustomIcon(color, iconClass) {
    return L.divIcon({
        className: 'custom-map-icon',
        html: `<div style="background-color: ${color}; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; border-radius: 50%; color: white; box-shadow: 0 4px 6px rgba(0,0,0,0.4); border: 2px solid white;">
                 <i class="${iconClass}" style="font-size: 13px;"></i>
               </div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 15],
        popupAnchor: [0, -15]
    });
}

// 1. CARREGAR DISTRITOS (BOROUGHS)
async function loadBoroughs() {
    try {
        const response = await fetch('/api/london-boundaries/geojson', { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Erro ao carregar distritos.');
        const geoJsonData = await response.json();

        L.geoJSON(geoJsonData, {
            style: { color: '#22c55e', weight: 2, fillColor: '#22c55e', fillOpacity: 0.08 },
            onEachFeature: (feature, layer) => {
                const props = feature.properties;
                layer.bindPopup(`<strong>Distrito:</strong> ${props.boroughName || 'N/A'}<br><em>Código:</em> ${props.boroughCode || 'N/A'}`);
            }
        }).addTo(boroughsGroup);
    } catch (error) {
        console.warn('Aviso nos Boroughs:', error);
    }
}

// 2. CARREGAR POSTOS E EMISSÕES
async function loadEmissionsMapData() {
    try {
        const response = await fetch('/api/emissions/map-latest', { headers: getAuthHeaders() });
        if (response.ok) {
            const stations = await response.json();
            stations.forEach(st => {
                const lat = st.latitude || st.lat;
                const lng = st.longitude || st.lng;
                const totalVoc = st.totalVocKg || st.vocEmissionKg || 12.4;
                const color = getSeverityColor(totalVoc, 'voc');

                if (lat && lng) {
                    const icon = createCustomIcon(color, 'fa-solid fa-gas-pump');
                    const marker = L.marker([lat, lng], { icon }).bindPopup(`
                        <div class="p-2 text-slate-900 min-w-[200px]">
                            <h4 class="font-bold text-slate-900 text-sm mb-1 flex items-center gap-1.5">
                                <i class="fa-solid fa-gas-pump text-emerald-600"></i> Posto ID #${st.gasStationId || st.id || 'N/A'}
                            </h4>
                            <p class="text-xs"><strong>Emissão Total VOC:</strong> <span class="font-bold" style="color: ${color}">${totalVoc.toFixed(2)} kg</span></p>
                            <p class="text-xs"><strong>Temperatura Local:</strong> ${st.temperatureCelsius || 15.0} °C</p>
                            <p class="text-xs"><strong>Velocidade do Vento:</strong> ${st.windSpeedKmh || 10.0} km/h</p>
                        </div>
                    `);
                    fuelGroup.addLayer(marker);
                }
            });
        }
    } catch (error) {
        console.error('Erro ao carregar postos:', error);
    }
}

// 3. CARREGAR ESTAÇÕES DE QUALIDADE DO AR
async function loadAirQualityStations() {
    try {
        const response = await fetch('/api/air-quality-stations', { headers: getAuthHeaders() });
        if (response.ok) {
            const airStations = await response.json();
            airStations.forEach(st => {
                let lat = st.latitude;
                let lng = st.longitude;
                if (!lat && st.location && st.location.coordinates) {
                    lng = st.location.coordinates[0];
                    lat = st.location.coordinates[1];
                }

                if (lat && lng) {
                    const icon = createCustomIcon('#0ea5e9', 'fa-solid fa-wind');
                    const marker = L.marker([lat, lng], { icon }).bindPopup(`
                        <div class="p-2 text-slate-900 min-w-[180px]">
                            <h4 class="font-bold text-sky-600 text-sm mb-1 flex items-center gap-1.5">
                                <i class="fa-solid fa-wind"></i> ${st.name || 'Estação LAQN'}
                            </h4>
                            <p class="text-xs"><strong>Código do Site:</strong> ${st.siteCode || 'N/A'}</p>
                            <p class="text-xs"><strong>Estado Operacional:</strong> <span class="text-emerald-600 font-semibold">${st.isActive !== false ? 'Ativo' : 'Inativo'}</span></p>
                        </div>
                    `);
                    airGroup.addLayer(marker);
                }
            });
        }
    } catch (error) {
        console.error('Erro ao carregar estações de ar:', error);
    }
}

// 4. CARREGAR PONTOS CRÍTICOS (HOTSPOTS)
async function loadHotspots() {
    try {
        const response = await fetch('/api/hotspots', { headers: getAuthHeaders() });
        if (response.ok) {
            const hotspots = await response.json();
            hotspots.forEach(h => {
                if (h.latitude && h.longitude) {
                    const color = getSeverityColor(h.severityLevel, 'severity');
                    const circle = L.circleMarker([h.latitude, h.longitude], {
                        radius: 9,
                        fillColor: color,
                        color: '#ffffff',
                        weight: 2,
                        fillOpacity: 0.9
                    }).bindPopup(`
                        <div class="p-2 text-slate-900">
                            <h4 class="font-bold text-rose-600 text-sm mb-1"><i class="fa-solid fa-triangle-exclamation"></i> Hotspot: ${h.pollutantType || 'VOC'}</h4>
                            <p class="text-xs"><strong>Severidade:</strong> <span class="uppercase font-bold" style="color: ${color}">${h.severityLevel || 'WARNING'}</span></p>
                            <p class="text-xs"><strong>Intensidade:</strong> ${h.intensityScore ? h.intensityScore.toFixed(2) : 'N/A'}</p>
                        </div>
                    `);
                    hotspotsGroup.addLayer(circle);
                }
            });
        }
    } catch (error) {
        console.warn('Aviso nos hotspots:', error);
    }
}

// 5. CARREGAR HEATMAP DE POLUIÇÃO
async function loadHeatmapData() {
    try {
        const response = await fetch('/api/spatial/heatmap-data', { headers: getAuthHeaders() });
        if (response.ok) {
            const data = await response.json();
            const heatPoints = data.map(pt => [pt.latitude, pt.longitude, pt.weight || 1.0]);

            if (heatmapLayer) {
                map.removeLayer(heatmapLayer);
            }

            if (typeof L.heatLayer === 'function') {
                heatmapLayer = L.heatLayer(heatPoints, {
                    radius: 22,
                    blur: 14,
                    maxZoom: 17,
                    gradient: { 0.4: 'blue', 0.65: 'lime', 0.85: 'orange', 1.0: 'red' }
                });

                if (document.getElementById('toggleHeatmap').checked) {
                    heatmapLayer.addTo(map);
                }
            }
        }
    } catch (error) {
        console.warn('Aviso no heatmap:', error);
    }
}

// Inicializar carregamento completo
loadBoroughs();
loadEmissionsMapData();
loadAirQualityStations();
loadHotspots();
loadHeatmapData();

// Controlo para Ativar/Desativar Camadas no Painel Flutuante
function toggleLayer(type) {
    if (type === 'heatmap') {
        if (!heatmapLayer) return;
        if (document.getElementById('toggleHeatmap').checked) heatmapLayer.addTo(map);
        else map.removeLayer(heatmapLayer);
    } else if (type === 'hotspots') {
        if (document.getElementById('toggleHotspots').checked) map.addLayer(hotspotsGroup);
        else map.removeLayer(hotspotsGroup);
    } else if (type === 'fuel') {
        if (document.getElementById('toggleFuel').checked) map.addLayer(fuelGroup);
        else map.removeLayer(fuelGroup);
    } else if (type === 'air') {
        if (document.getElementById('toggleAir').checked) map.addLayer(airGroup);
        else map.removeLayer(airGroup);
    } else if (type === 'boroughs') {
        if (document.getElementById('toggleBoroughs').checked) map.addLayer(boroughsGroup);
        else map.removeLayer(boroughsGroup);
    }
}

// Ecrã Inteiro
function toggleFullscreen() {
    const elem = document.getElementById('map-fullscreen-wrapper');
    if (!document.fullscreenElement) {
        elem.requestFullscreen().catch(err => alert(`Erro: ${err.message}`));
    } else {
        document.exitFullscreen();
    }
}

// Logout
function logout() {
    localStorage.removeItem('jwt_token');
    window.location.replace('/index.html');
}