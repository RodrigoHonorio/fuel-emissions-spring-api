// =================================================================
// S.P.I.R.E. - Módulo de Divisão Territorial (london-borders.js)
// =================================================================

console.log("🗺️ A carregar os limites dos distritos locais...");

// Cria o grupo de camadas no mapa
const londonBordersLayer = L.layerGroup().addTo(window.map);

// Procura o ficheiro GeoJSON guardado na pasta local
fetch('./london-boroughs.geojson')
    .then(response => {
        if (!response.ok) {
            throw new Error(`Erro ao carregar o ficheiro local: ${response.statusText}`);
        }
        return response.json();
    })
    .then(geojsonData => {
        const londonGeoJsonLayer = L.geoJSON(geojsonData, {
            style: function () {
                return {
                    color: '#1565C0',       // Cor azul forte para destacar a borda
                    weight: 2,              // Espessura da linha
                    opacity: 0.85,          // Opacidade do contorno
                    fillColor: '#2196F3',   // Preenchimento interior
                    fillOpacity: 0.08,      // Opacidade suave para não tapar os postos
                    dashArray: '6, 4'       // Linha tracejada
                };
            },
            onEachFeature: function (feature, layer) {
                const props = feature.properties || {};
                const councilName = props.name || props.NAME || props.borough || "Distrito de Londres";

                // Adiciona Pop-up ao clicar no distrito
                layer.bindPopup(`
                    <div style="font-family: Arial, sans-serif; text-align: center; padding: 4px;">
                        <span style="font-size: 10px; color: #7f8c8d; text-transform: uppercase; font-weight: bold;">Borough / Distrito</span><br>
                        <b style="font-size: 14px; color: #1565C0;">${councilName}</b>
                    </div>
                `);

                // Efeito ao passar o rato por cima
                layer.on({
                    mouseover: function (e) {
                        e.target.setStyle({
                            color: '#D32F2F', // Vermelho no hover
                            weight: 3,
                            fillOpacity: 0.2
                        });
                    },
                    mouseout: function (e) {
                        londonGeoJsonLayer.resetStyle(e.target);
                    }
                });
            }
        }).addTo(londonBordersLayer);

        // Garante que o desenho das fronteiras fica atrás dos pinos de combustível
        londonGeoJsonLayer.bringToBack();
        console.log("✅ Limites dos distritos desenhados com sucesso no mapa!");
    })
    .catch(error => {
        console.error("❌ Erro ao carregar london-boroughs.geojson:", error);
    });