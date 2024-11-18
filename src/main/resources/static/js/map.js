class MapManager {
  constructor() {
    this.map = null;
    this.drawControl = null;
    this.drawnItems = null;
    this.vectorGridLayer = null;
  }

  initialize() {
    this.map = L.map("map").setView([53.4808, -2.2487], 13);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "© OpenStreetMap contributors",
    }).addTo(this.map);

    this.drawnItems = new L.FeatureGroup();
    this.map.addLayer(this.drawnItems);

    this.setupDrawControls(false);
  }

  setupDrawControls(enabled) {
    if (this.drawControl) {
      this.map.removeControl(this.drawControl);
    }

    if (enabled) {
      this.drawControl = new L.Control.Draw({
        draw: {
          polygon: {
            allowIntersection: false,
            drawError: {
              color: "#e1e100",
              timeout: 1000,
            },
            shapeOptions: {
              color: "#3388ff",
            },
            showArea: true,
          },
          circle: false,
          circlemarker: false,
          marker: false,
          polyline: false,
          rectangle: false,
        },
        edit: {
          featureGroup: this.drawnItems,
          remove: true,
        },
      });

      this.map.addControl(this.drawControl);
    }
  }

  clearDrawings() {
    this.drawnItems.clearLayers();
  }

  getDrawnPolygon() {
    let geojson = null;
    this.drawnItems.eachLayer((layer) => {
      if (layer instanceof L.Polygon) {
        geojson = layer.toGeoJSON();
      }
    });
    return geojson;
  }

  displayTiles(geojsonData) {
    if (this.vectorGridLayer) {
      this.map.removeLayer(this.vectorGridLayer);
    }

    this.vectorGridLayer = L.vectorGrid
      .slicer(geojsonData, {
        rendererFactory: L.canvas.tile,
        vectorTileLayerStyles: {
          sliced: {
            weight: 1,
            color: "#FF4444",
            fillColor: "#FF4444",
            fillOpacity: 0.2,
          },
        },
        interactive: true,
        maxZoom: 19,
      })
      .addTo(this.map);

    if (geojsonData.features && geojsonData.features.length > 0) {
      const bounds = turf.bbox(geojsonData);
      this.map.fitBounds([
        [bounds[1], bounds[0]],
        [bounds[3], bounds[2]],
      ]);
    }
  }

  enableDrawing() {
    this.setupDrawControls(true);
    document.querySelector(".draw-instructions").classList.add("active");
  }

  disableDrawing() {
    this.setupDrawControls(false);
    document.querySelector(".draw-instructions").classList.remove("active");
  }
}

export const mapManager = new MapManager();
