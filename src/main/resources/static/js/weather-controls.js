/**
 * Módulo de Eventos dos Controlos Meteorológicos
 * Escuta as interações do utilizador e sincroniza a atualização do mapa.
 */
document.addEventListener('DOMContentLoaded', () => {

    // Referências dos Elementos
    const tempInput = document.getElementById('tempInput');
    const tempValue = document.getElementById('tempValue');

    const windSpeedInput = document.getElementById('windSpeedInput');
    const windSpeedValue = document.getElementById('windSpeedValue');

    const windDirInput = document.getElementById('windDirInput');
    const windDirValue = document.getElementById('windDirValue');

    const fuelTypeSelect = document.getElementById('fuelTypeSelect');
    const btnRecalculate = document.getElementById('btnRecalculate');

    const toggleHeatmap = document.getElementById('toggleHeatmap');
    const togglePlumes = document.getElementById('togglePlumes');

    // Atualização dos Rótulos em Tempo Real durante o Slider
    tempInput.addEventListener('input', (e) => tempValue.textContent = e.target.value);
    windSpeedInput.addEventListener('input', (e) => windSpeedValue.textContent = e.target.value);
    windDirInput.addEventListener('input', (e) => windDirValue.textContent = e.target.value);

    // Função que recolhe os parâmetros atuais e dispara a chamada de atualização
    function triggerUpdate() {
        const params = {
            temp: parseFloat(tempInput.value),
            windSpeed: parseFloat(windSpeedInput.value),
            windDirection: parseFloat(windDirInput.value),
            fuelType: fuelTypeSelect.value
        };

        fetchAndRenderVocData(params);
    }

    // Eventos de clique e toggles
    btnRecalculate.addEventListener('click', triggerUpdate);
    toggleHeatmap.addEventListener('change', triggerUpdate);
    togglePlumes.addEventListener('change', triggerUpdate);

    // Carga inicial ao abrir a página
    triggerUpdate();
});