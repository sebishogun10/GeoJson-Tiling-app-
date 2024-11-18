class TileManager {
  constructor() {
    this.startTime = null;
    this.renderedFeatures = [];
  }

  updateProgress(processed, total) {
    const percentage = (processed / total) * 100;
    const progressBarFill = document.getElementById("progressBarFill");
    const tilesGenerated = document.getElementById("tilesGenerated");
    const timeEstimate = document.getElementById("timeEstimate");

    progressBarFill.style.width = `${percentage}%`;
    tilesGenerated.textContent = processed;

    if (!this.startTime) this.startTime = Date.now();
    const elapsed = (Date.now() - this.startTime) / 1000;
    const rate = processed / elapsed;
    const remaining = (total - processed) / rate;

    timeEstimate.textContent = `Estimated time remaining: ${Math.round(
      remaining
    )}s`;
  }

  startLoading() {
    document.getElementById("loadingOverlay").classList.remove("hidden");
    this.startTime = Date.now();
    this.updateProgress(0, 100);
  }

  finishLoading() {
    document.getElementById("loadingOverlay").classList.add("hidden");
    document.getElementById(
      "stats"
    ).textContent = `Total tiles: ${this.renderedFeatures.length}`;
  }

  async generateTiles(geoJson, token) {
    const requestData = {
      geoJson: geoJson,
      maxTileArea: parseFloat(document.getElementById("maxTileArea").value),
      minTileArea: parseFloat(document.getElementById("minTileArea").value),
      coverageThreshold: parseFloat(
        document.getElementById("coverageThreshold").value
      ),
      includeBoundingBox: document.getElementById("includeBoundingBox").checked,
    };

    this.startLoading();

    try {
      const response = await fetch("/api/v1/tiles", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(requestData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `HTTP error! status: ${response.status}, message: ${errorText}`
        );
      }

      const data = await response.json();

      if (data && data.features) {
        this.renderedFeatures = data.features;
        this.updateProgress(
          this.renderedFeatures.length,
          this.renderedFeatures.length
        );
        this.finishLoading();
        return data;
      }

      throw new Error("Invalid response format");
    } catch (error) {
      console.error("Error:", error);
      document.getElementById("stats").textContent = `Error: ${error.message}`;
      document.getElementById("loadingOverlay").classList.add("hidden");
      throw error;
    }
  }
}

export const tileManager = new TileManager();
