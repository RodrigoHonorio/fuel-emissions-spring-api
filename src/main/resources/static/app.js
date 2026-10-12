/**
 * Greater London Dashboard - Core Logic
 * Arquitetura: Module Pattern c/ State Management simples.
 */

// ==========================================
// 1. Configurações Globais & Estado
// ==========================================
const API_BASE = 'http://localhost:8080'; // Ajuste conforme ambiente
const ENDPOINTS = {
    boroughs: `${API_BASE}/api/boroughs`,
    gasStations: `${API_BASE}/api/v1/map/gas-stations`,
    airStations: `${API_BASE}/api/v1/map/air-stations`,
    polling: `${API_BASE}/api/backfill/progress`
};

const appState = {
    map: null,
    layers: {
        boroughs: null,
        gasStations: null,
        airStations: null
    },
    dataCache: {
        gasStations: null // Cache para evitar fetch ao alternar o toggle científico
    },
    flags: {
        isScientificActive: false
    }
};

// ==========================================
// 2. Inicialização do Mapa
// ==========================================
function initMap() {
    // Centro de Londres
    appState.map = L.map('map', { zoomControl: false }).setView([51.505, -0.09], 11);

    // Controle de Zoom movido para direita (não conflitar com a sidebar)
    L.control.zoom({ position: 'topright' }).addTo(appState.map);

    // Basemap: Esri Light Gray Canvas (Minimalista, focado em dados)
    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
        attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ',
        maxZoom: 16
    }).addTo(appState.map);
}

// ==========================================
// 3. Sistema de Fetch com Tratamento de Erro
// ==========================================
async function fetchGeoData(url, layerName) {
    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP Error ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error(`[GeoData Error] Falha ao carregar ${layerName}:`, error);
        // Em um ambiente real, acionar um Toast Notification aqui (ex: Toastify)
        return null;
    }
}

// ==========================================
// 4. Renderização de Camadas (Layers)
// ==========================================

// 4.1. Limites dos Distritos (London Boundaries)
async function loadBoroughs() {
    const data = await fetchGeoData(ENDPOINTS.boroughs, 'Boroughs');
    if (!data) return;

    appState.layers.boroughs = L.geoJSON(data, {
        style: {
            color: '#94a3b8', // slate-400
            weight: 1,
            fillColor: '#cbd5e1',
            fillOpacity: 0.1,
            dashArray: '4'
        },
        onEachFeature: (feature, layer) => {
            layer.bindTooltip(`<b>${feature.properties.name}</b>`, { className: 'bg-slate-800 text-white border-none' });
        }
    }).addTo(appState.map);
}

// 4.2. Estações de Qualidade do Ar (LAQN)
async function loadAirStations() {
    const data = await fetchGeoData(ENDPOINTS.airStations, 'Air Stations');
    if (!data) return;

    appState.layers.airStations = L.geoJSON(data, {
        pointToLayer: (feature, latlng) => {
            return L.circleMarker(latlng, {
                radius: 6,
                fillColor: '#10b981', // emerald-500
                color: '#fff',
                weight: 1,
                fillOpacity: 0.8
            });
        },
        onEachFeature: (feature, layer) => {
            const p = feature.properties;
            const popupContent = `
                <div class="p-2 min-w-[200px]">
                    <h3 class="font-bold text-emerald-400 mb-2 border-b border-slate-600 pb-1">${p.name || 'Estação LAQN'}</h3>
                    <div class="grid grid-cols-2 gap-2 text-sm text-slate-300">
                        <div>NO2: <span class="font-mono text-white">${p.no2}</span></div>
                        <div>PM2.5: <span class="font-mono text-white">${p.pm25}</span></div>
                        <div>O3: <span class="font-mono text-white">${p.o3}</span></div>
                        <div>Status: <span class="text-${p.active ? 'emerald' : 'red'}-400">${p.active ? 'Ativa' : 'Inativa'}</span></div>
                    </div>
                </div>
            `;
            layer.bindPopup(popupContent, { className: 'custom-popup' });
        }
    }).addTo(appState.map);
}

// 4.3. Postos de Combustível e Motor Científico S.P.I.R.E.
async function loadGasStations() {
    // Usa cache se existir, para otimizar o toggle científico
    const data = appState.dataCache.gasStations || await fetchGeoData(ENDPOINTS.gasStations, 'Gas Stations');
    if (!data) return;

    appState.dataCache.gasStations = data;

    // Remove camada antiga se existir (útil para o toggle)
    if (appState.layers.gasStations) {
        appState.map.removeLayer(appState.layers.gasStations);
    }

    appState.layers.gasStations = L.geoJSON(data, {
        pointToLayer: (feature, latlng) => {
            const p = feature.properties;
            const isScientific = appState.flags.isScientificActive;

            // Lógica Visual: Se modelo científico ativado, usa a cor de risco. Senão, azul genérico.
            const fillColor = isScientific && p.spire_risk_color ? p.spire_risk_color : '#0ea5e9'; // sky-500
            const radius = isScientific ? 8 : 5;

            return L.circleMarker(latlng, {
                radius: radius,
                fillColor: fillColor,
                color: '#1e293b',
                weight: 2,
                fillOpacity: 0.9
            });
        },
        onEachFeature: (feature, layer) => {
            const p = feature.properties;
            const isScientific = appState.flags.isScientificActive;

            // Lógica do Popup Dinâmico
            let popupContent = `<div class="p-2 min-w-[220px]">
                <h3 class="font-bold text-sky-400 mb-1">${p.name || 'Posto CMA'}</h3>
                <p class="text-xs text-slate-400 mb-3">${p.address || 'Sem endereço registrado'}</p>`;

            if (isScientific && p.spire_voc_kg) {
                // Conteúdo estendido S.P.I.R.E.
                popupContent += `
                    <div class="bg-slate-900 rounded p-2 text-xs border border-slate-700">
                        <div class="flex justify-between mb-1">
                            <span class="text-slate-400">Modelo:</span>
                            <span class="text-sky-300 font-bold">CETESB 2015</span>
                        </div>
                        <div class="flex justify-between mb-1">
                            <span class="text-slate-400">Emissão VOC:</span>
                            <span class="text-white font-mono">${parseFloat(p.spire_voc_kg).toFixed(2)} kg/h</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-slate-400">Risco:</span>
                            <span class="font-bold uppercase" style="color: ${p.spire_risk_color || '#fff'}">${p.spire_risk_level || 'N/A'}</span>
                        </div>
                    </div>`;
            } else {
                // Conteúdo Básico
                popupContent += `
                    <div class="text-sm text-slate-300">
                        Status: <span class="text-${p.active ? 'emerald' : 'red'}-400">${p.active ? 'Operacional' : 'Fechado'}</span><br>
                        Combustível Base: £ ${p.price_base || 'N/A'}
                    </div>`;
            }

            popupContent += `</div>`;
            layer.bindPopup(popupContent, { className: 'custom-popup' });
        }
    }).addTo(appState.map);
}

// ==========================================
// 5. Polling Progressivo de Background Tasks
// ==========================================
function startBackfillPolling() {
    const container = document.getElementById('polling-container');
    const emptyMsg = document.getElementById('polling-empty');

    setInterval(async () => {
        try {
            const response = await fetch(ENDPOINTS.polling);
            if (!response.ok) return;
            const tasks = await response.json();

            container.innerHTML = ''; // Limpa atual

            if (tasks.length === 0) {
                container.appendChild(emptyMsg);
                emptyMsg.style.display = 'block';
                return;
            }

            emptyMsg.style.display = 'none';

            tasks.forEach(task => {
                const progressHtml = `
                    <div class="mb-3">
                        <div class="flex justify-between text-xs mb-1">
                            <span class="text-slate-300">${task.name}</span>
                            <span class="text-emerald-400">${task.percentage}%</span>
                        </div>
                        <div class="w-full bg-slate-700 rounded-full h-1.5">
                            <div class="bg-emerald-500 h-1.5 rounded-full transition-all duration-500" style="width: ${task.percentage}%"></div>
                        </div>
                    </div>
                `;
                container.insertAdjacentHTML('beforeend', progressHtml);
            });
        } catch (e) {
            // Silencioso para não poluir console caso endpoint backend esteja offline
        }
    }, 5000); // Poll a cada 5 segundos
}

// ==========================================
// 6. Manipuladores de Eventos (UI)
// ==========================================

// Alternar abas da Sidebar
window.switchTab = function(tabName) {
    const btnControls = document.getElementById('tab-controls');
    const btnMethodology = document.getElementById('tab-methodology');
    const panelControls = document.getElementById('panel-controls');
    const panelMethodology = document.getElementById('panel-methodology');

    if (tabName === 'controls') {
        btnControls.classList.replace('text-slate-400', 'text-emerald-500');
        btnControls.classList.add('border-b-2', 'border-emerald-500', 'bg-slate-800/50');
        btnMethodology.classList.remove('text-emerald-500', 'border-b-2', 'border-emerald-500', 'bg-slate-800/50');
        btnMethodology.classList.add('text-slate-400');

        panelControls.classList.remove('hidden');
        panelMethodology.classList.add('hidden');
    } else {
        btnMethodology.classList.replace('text-slate-400', 'text-emerald-500');
        btnMethodology.classList.add('border-b-2', 'border-emerald-500', 'bg-slate-800/50');
        btnControls.classList.remove('text-emerald-500', 'border-b-2', 'border-emerald-500', 'bg-slate-800/50');
        btnControls.classList.add('text-slate-400');

        panelMethodology.classList.remove('hidden');
        panelControls.classList.add('hidden');
    }
};

function setupEventListeners() {
    // Toggles de Camadas (Ligam/Desligam do Mapa)
    const toggleLayer = (checkboxId, layerKey) => {
        document.getElementById(checkboxId).addEventListener('change', (e) => {
            if (!appState.layers[layerKey]) return;
            e.target.checked
                ? appState.map.addLayer(appState.layers[layerKey])
                : appState.map.removeLayer(appState.layers[layerKey]);
        });
    };

    toggleLayer('toggle-boroughs', 'boroughs');
    toggleLayer('toggle-air-stations', 'airStations');
    toggleLayer('toggle-gas-stations', 'gasStations');

    // Toggle do Motor Científico S.P.I.R.E.
    document.getElementById('toggle-scientific').addEventListener('change', (e) => {
        appState.flags.isScientificActive = e.target.checked;

        // Se a camada principal de postos estiver desligada, liga automaticamente
        const gasToggle = document.getElementById('toggle-gas-stations');
        if (!gasToggle.checked) {
            gasToggle.checked = true;
        }

        // Re-renderiza a camada de postos aplicando (ou removendo) a simbologia científica
        loadGasStations();
    });
}

// ==========================================
// 7. Bootstrapping
// ==========================================
document.addEventListener('DOMContentLoaded', async () => {
    initMap();
    setupEventListeners();

    // Carregamento assíncrono paralelo para otimizar TTI (Time to Interactive)
    await Promise.allSettled([
        loadBoroughs(),
        loadAirStations(),
        loadGasStations()
    ]);

    startBackfillPolling();
});