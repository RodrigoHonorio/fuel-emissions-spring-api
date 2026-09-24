/**
 * Módulo de Inicialização do Mapa Leaflet
 * Utiliza o OpenStreetMap público sem necessidade de API Key.
 */
const map = L.map('map', {
    center: [51.5074, -0.1278], // Centro de Londres
    zoom: 11,
    zoomControl: true
});

// Camada de Tile 100% gratuita do OpenStreetMap
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
}).addTo(map);