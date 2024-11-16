package com.example.tilingservice.controller;

import com.example.tilingservice.service.AsyncTileRenderer;
import com.example.tilingservice.service.TileService;
import com.example.tilingservice.tile.Tile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Tiling Controller", description = "API endpoints for polygon tiling operations")
@CrossOrigin(origins = "http://localhost:3000")
public class TilingController {

    private final TileService tileService;
    private final AsyncTileRenderer asyncTileRenderer;

    @PostMapping("/tiles")
    @Operation(
        summary = "Generate tiles for an area of interest",
        description = "Takes a GeoJSON polygon and returns a list of tile footprints",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<String> generateTiles(
            @RequestBody String geoJson,
            @AuthenticationPrincipal Jwt jwt) {
        
        List<Tile> tiles = tileService.generateTiles(geoJson);
        // String response = tileService.generateGeoJson(tiles);
        String response = asyncTileRenderer.renderTilesAsync(tiles);
        
        return ResponseEntity.ok(response);
    }
}
