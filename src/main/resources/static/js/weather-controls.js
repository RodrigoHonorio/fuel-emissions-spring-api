/**
 * Módulo de Eventos dos Controlos Meteorológicos
 */
document.addEventListener('DOMContentLoaded', () => {

    const fuelTypeSelect = document.getElementById('fuelTypeSelect');
    const btnRecalculate = document.getElementById('btnRecalculate');
    const toggleHeatmap = document.getElementById('toggleHeatmap');
    const toggleStations = document.getElementById('toggleStations');

    // Elementos da Rosa dos Ventos
    const compassArrow = document.getElementById('compassArrow');
    const uiWindSpeed = document.getElementById('wt-wind-speed');
    const uiTemp = document.getElementById('wt-temp');
    const uiDir = document.getElementById('wt-dir');
    const uiHum = document.getElementById('wt-hum');
    const uiUv = document.getElementById('wt-uv');

    let currentWeather = {
        temp: 20.0, windSpeed: 3.5, windDirection: 240, humidity: 65, uv: 4
    };

    async function fetchRealtimeWeather() {
        try {
            const response = await fetch('/api/v1/spatial-nodes?size=5');
            if (response.ok) {
                const data = await response.json();
                const nodes = Array.isArray(data) ? data : (data.content || []);

                if (nodes.length > 0) {
                    const node = nodes[0];
                    currentWeather = {
                        temp: node.ambientTemperatureCelsius ?? currentWeather.temp,
                        windSpeed: node.windSpeed ?? currentWeather.windSpeed,
                        windDirection: node.windDirection ?? currentWeather.windDirection,
                        humidity: node.humidity ?? currentWeather.humidity,
                        uv: node.uvIndex ?? currentWeather.uv
                    };
                }
            }
        } catch (error) {
            console.error("⚠️ Erro API meteorológica. A usar valores por defeito.");
        }

        updateWeatherUI(currentWeather);
        triggerUpdate();
    }

    function updateWeatherUI(data) {
        uiTemp.textContent = `${data.temp.toFixed(1)}°C`;
        uiWindSpeed.textContent = data.windSpeed.toFixed(1);
        uiDir.textContent = `${data.windDirection}°`;
        uiHum.textContent = `${data.humidity}%`;
        uiUv.textContent = data.uv;

        const arrowRotation = data.windDirection + 180;
        compassArrow.style.transform = `translate(-50%, -50%) rotate(${arrowRotation}deg)`;
    }

    function triggerUpdate() {
        const params = {
            temp: currentWeather.temp,
            windSpeed: currentWeather.windSpeed,
            windDirection: currentWeather.windDirection,
            humidity: currentWeather.humidity,
            fuelType: fuelTypeSelect.value
        };
        fetchAndRenderVocData(params);
    }

    // Liga os Eventos
    if (btnRecalculate) btnRecalculate.addEventListener('click', triggerUpdate);
    if (toggleHeatmap) toggleHeatmap.addEventListener('change', triggerUpdate);
    if (fuelTypeSelect) fuelTypeSelect.addEventListener('change', triggerUpdate);

    // Controlo da Checkbox para ligar/desligar Postos
    if (toggleStations) {
        toggleStations.addEventListener('change', (e) => {
            if (typeof window.toggleGasStationsLayer === 'function') {
                window.toggleGasStationsLayer(e.target.checked ? 'both' : 'none');
            }
            if (typeof window.toggleEnvironmentLayer === 'function') {
                window.toggleEnvironmentLayer(e.target.checked ? 'both' : 'none');
            }
        });
    }

    fetchRealtimeWeather();
});