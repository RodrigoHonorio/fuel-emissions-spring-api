/**
 * Módulo de Inicialização do Mapa Leaflet
 */
const map = L.map('map', {
    center: [51.5074, -0.1278], // Centro de Londres
    zoom: 11,
    zoomControl: true
});

// Esri World Light Gray Canvas (Limpo, elegante e 100% grátis sem chave)
L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ',
    maxZoom: 16
}).addTo(map);

// ESSENCIAL: Exportar o mapa para que os outros scripts consigam aceder
window.map = map;