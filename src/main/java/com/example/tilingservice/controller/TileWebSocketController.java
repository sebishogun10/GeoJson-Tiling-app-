package com.example.tilingservice.controller;

import com.example.tilingservice.service.AsyncTileRenderer;
import com.example.tilingservice.service.TileService;
import com.example.tilingservice.tile.Tile;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TileWebSocketController {
    private final TileService tileService;
    private final AsyncTileRenderer tileRenderer;

    @Data
    public static class WebSocketTilingRequest {
        @JsonProperty("geoJson")
        private String geoJson;
        
        @JsonProperty("maxTileArea")
        private double maxTileArea = 1000.0;
        
        @JsonProperty("minTileArea")
        private double minTileArea = 10.0;
        
        @JsonProperty("coverageThreshold")
        private double coverageThreshold = 0.10;
        
        @JsonProperty("includeBoundingBox")
        private boolean includeBoundingBox = true;
    }

    @MessageMapping("/tiles")
    @SendTo("/topic/tiles")
    public void streamTiles(WebSocketTilingRequest request) {
        List<Tile> tiles = tileService.generateTiles(
            request.getGeoJson(),
            request.getMaxTileArea(),
            request.getMinTileArea(),
            request.getCoverageThreshold(),
            request.isIncludeBoundingBox()
        );
        tileRenderer.streamTiles(tiles);
    }
}