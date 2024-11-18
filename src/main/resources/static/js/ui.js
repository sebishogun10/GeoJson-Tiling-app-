export class UIManager {
  constructor(mapManager, authManager, tileManager) {
    this.mapManager = mapManager;
    this.authManager = authManager;
    this.tileManager = tileManager;
    this.currentTilesData = null;
    this.geoJsonTabBtn = document.getElementById("geoJsonTabBtn");
    this.drawTabBtn = document.getElementById("drawTabBtn");
    this.geoJsonSection = document.getElementById("geoJsonInput-section");
    this.drawSection = document.getElementById("draw-section");
    this.generateBtn = document.getElementById("generateBtn");
    this.downloadTilesBtn = document.getElementById("downloadTilesBtn");
    this.clearTilesBtn = document.getElementById("clearTilesBtn");
    this.startDrawingBtn = document.getElementById("startDrawingBtn");
    this.clearDrawingBtn = document.getElementById("clearDrawingBtn");
  }

  initialize() {
    this.setupTabSwitching();
    this.setupButtons();
    this.setupDrawingControls();
  }

  setupTabSwitching() {
    if (this.geoJsonTabBtn && this.drawTabBtn) {
      this.geoJsonTabBtn.addEventListener("click", () =>
        this.switchToGeoJsonMode()
      );
      this.drawTabBtn.addEventListener("click", () => this.switchToDrawMode());
    }
  }

  switchToGeoJsonMode() {
    this.geoJsonTabBtn.classList.add("active", "bg-blue-500", "text-white");
    this.geoJsonTabBtn.classList.remove("bg-gray-200");
    this.drawTabBtn.classList.remove("active", "bg-blue-500", "text-white");
    this.drawTabBtn.classList.add("bg-gray-200");
    this.geoJsonSection.classList.remove("hidden");
    this.geoJsonSection.classList.add("active");
    this.drawSection.classList.add("hidden");
    this.drawSection.classList.remove("active");
    this.mapManager.disableDrawing();
  }

  switchToDrawMode() {
    this.drawTabBtn.classList.add("active", "bg-blue-500", "text-white");
    this.drawTabBtn.classList.remove("bg-gray-200");
    this.geoJsonTabBtn.classList.remove("active", "bg-blue-500", "text-white");
    this.geoJsonTabBtn.classList.add("bg-gray-200");
    this.drawSection.classList.remove("hidden");
    this.drawSection.classList.add("active");
    this.geoJsonSection.classList.add("hidden");
    this.geoJsonSection.classList.remove("active");
  }

  setupButtons() {
    if (this.generateBtn) {
      this.generateBtn.addEventListener("click", () => this.handleGenerate());
    }

    if (this.clearTilesBtn) {
      this.clearTilesBtn.addEventListener("click", () => {
        this.mapManager.clearTiles();
        this.downloadTilesBtn.classList.add("hidden");
        this.currentTilesData = null;
      });
    }

    if (this.downloadTilesBtn) {
      this.downloadTilesBtn.addEventListener("click", () => {
        if (this.currentTilesData) {
          const timestamp = new Date()
            .toISOString()
            .replace(/[^0-9]/g, "")
            .slice(0, -3);
          this.downloadGeoJson(
            this.currentTilesData,
            `tiles_${timestamp}.geojson`
          );
        }
      });
    }
  }

  setupDrawingControls() {
    if (this.startDrawingBtn) {
      this.startDrawingBtn.addEventListener("click", () => {
        this.mapManager.enableDrawing();
      });
    }

    if (this.clearDrawingBtn) {
      this.clearDrawingBtn.addEventListener("click", () => {
        this.mapManager.clearDrawings();
      });
    }
  }

  async handleGenerate() {
    let geoJson;

    if (this.geoJsonSection.classList.contains("hidden")) {
      geoJson = this.mapManager.getDrawnPolygon();
      if (!geoJson) {
        alert("Please draw a polygon first");
        return;
      }
    } else {
      try {
        const inputJson = JSON.parse(
          document.getElementById("geoJsonInput").value
        );

        if (inputJson.type === "Feature" && inputJson.geometry) {
          geoJson = inputJson;
        } else if (
          inputJson.type === "Polygon" ||
          inputJson.type === "MultiPolygon"
        ) {
          geoJson = {
            type: "Feature",
            geometry: inputJson,
            properties: {},
          };
        } else {
          throw new Error("Invalid GeoJSON format");
        }
      } catch (e) {
        alert("Invalid GeoJSON format");
        return;
      }
    }

    if (!this.authManager.getToken()) {
      alert("Please log in first");
      return;
    }

    const requestData = {
      geoJson: geoJson,
      maxTileArea: parseFloat(document.getElementById("maxTileArea").value),
      minTileArea: parseFloat(document.getElementById("minTileArea").value),
      coverageThreshold: parseFloat(
        document.getElementById("coverageThreshold").value
      ),
      includeBoundingBox: document.getElementById("includeBoundingBox").checked,
    };

    try {
      const data = await this.tileManager.generateTiles(
        requestData,
        this.authManager.getToken()
      );
      if (data) {
        this.currentTilesData = data;
        this.downloadTilesBtn.classList.remove("hidden");
        this.mapManager.displayTiles(data);
      }
    } catch (error) {
      alert("Error generating tiles: " + error.message);
    }
  }

  downloadGeoJson(geojsonData, filename = "tiles.geojson") {
    const data = JSON.stringify(geojsonData, null, 2);
    const blob = new Blob([data], { type: "application/json" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  }
}
