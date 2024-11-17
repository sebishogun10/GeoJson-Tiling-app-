package com.example.tilingservice.controller;

import com.example.tilingservice.service.AsyncTileRenderer;
import com.example.tilingservice.service.TileService;
import com.example.tilingservice.tile.Tile;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Tiling Controller", description = "API endpoints for polygon tiling operations")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
@Slf4j
public class TilingController {

    private final TileService tileService;
    private final AsyncTileRenderer asyncTileRenderer;
    private final ObjectMapper objectMapper;

    @Data
    public static class TilingRequest {
        @JsonProperty("geoJson")
        private JsonNode geoJson;
        
        @JsonProperty("maxTileArea")
        private double maxTileArea = 1000.0;
        
        @JsonProperty("minTileArea")
        private double minTileArea = 10.0;
        
        @JsonProperty("coverageThreshold")
        private double coverageThreshold = 0.10;
        
        @JsonProperty("includeBoundingBox")
        private boolean includeBoundingBox = true;
    }

    @PostMapping("/tiles")
    @Operation(
        summary = "Generate tiles for an area of interest",
        description = "Takes a GeoJSON polygon and tiling parameters, returns a list of tile footprints",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<String> generateTiles(
            @RequestBody TilingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String geoJsonString = objectMapper.writeValueAsString(request.getGeoJson());
            
            List<Tile> tiles = tileService.generateTiles(
                geoJsonString,
                request.getMaxTileArea(),
                request.getMinTileArea(),
                request.getCoverageThreshold(),
                request.isIncludeBoundingBox()
            );
            
            String response = asyncTileRenderer.renderTilesAsync(tiles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing request: ", e);
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"UP\"}");
    }
}
