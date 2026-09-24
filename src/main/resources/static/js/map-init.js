/**
 * Módulo de Inicialização do Mapa Leaflet
 */
const map = L.map('map', {
    center: [51.5074, -0.1278], // Centro de Londres
    zoom: 11,
    zoomControl: true
});

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
}).addTo(map);

// ESSENCIAL: Exportar o mapa para que stations.js e gas-stations.js consigam desenhar os ícones nele
window.map = map;