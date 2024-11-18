class TileManager {
    constructor() {
        this.startTime = null;
        this.renderedFeatures = [];
        this.loadingOverlay = document.getElementById('loadingOverlay');
        this.progressBar = document.getElementById('progressBarFill');
        this.tilesGenerated = document.getElementById('tilesGenerated');
        this.timeEstimate = document.getElementById('timeEstimate');
        this.stats = document.getElementById('stats');
    }

    updateProgress(processed, total, estimatedTotal = null) {
        const actualTotal = estimatedTotal || total;
        const percentage = Math.min((processed / actualTotal) * 100, 100);
        
        this.progressBar.style.width = `${percentage}%`;
        this.tilesGenerated.textContent = processed;
        
        if (!this.startTime) this.startTime = Date.now();
        const elapsed = (Date.now() - this.startTime) / 1000;
        const rate = processed / elapsed;
        
        if (rate > 0) {
            const remaining = (actualTotal - processed) / rate;
            this.timeEstimate.textContent = `Estimated time remaining: ${Math.round(remaining)}s`;
            this.stats.textContent = `Processing rate: ${Math.round(rate)} tiles/sec`;
        }
    }

    startLoading() {
        this.loadingOverlay.classList.remove("hidden");
        this.startTime = Date.now();
        this.renderedFeatures = [];
        this.updateProgress(0, 100);
        this.progressBar.style.width = '0%';
    }

    finishLoading(totalTiles) {
        this.loadingOverlay.classList.add("hidden");
        this.stats.textContent = `Total tiles generated: ${totalTiles}`;
        
        const endTime = Date.now();
        const totalTime = (endTime - this.startTime) / 1000;
        const finalRate = totalTiles / totalTime;
        
        console.log(`Generation completed in ${totalTime.toFixed(2)}s, Rate: ${finalRate.toFixed(2)} tiles/sec`);
    }

    async generateTiles(requestData, token) {
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
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }

            let reader = response.body.getReader();
            let decoder = new TextDecoder();
            let buffer = '';
            let tileCount = 0;
            let jsonResponse = null;

            while (true) {
                const { done, value } = await reader.read();
                
                if (done) {
                    if (buffer) {
                        try {
                            jsonResponse = JSON.parse(buffer);
                        } catch (e) {
                            console.error("Error parsing final JSON:", e);
                        }
                    }
                    break;
                }

                buffer += decoder.decode(value, { stream: true });
                tileCount = (buffer.match(/"type":"Feature"/g) || []).length;
                this.updateProgress(tileCount, Math.max(100, tileCount + 10));
            }

            if (jsonResponse && jsonResponse.features) {
                this.renderedFeatures = jsonResponse.features;
                this.updateProgress(this.renderedFeatures.length, this.renderedFeatures.length);
                this.finishLoading(this.renderedFeatures.length);
                return jsonResponse;
            }
            
            throw new Error("Invalid response format");
        } catch (error) {
            this.loadingOverlay.classList.add("hidden");
            this.stats.textContent = `Error: ${error.message}`;
            throw error;
        }
    }
}

export const tileManager = new TileManager();
