package com.example.tilingservice.service;

import com.example.tilingservice.geojson.Parser;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.model.Shape;
import com.example.tilingservice.rtree.RTree;
import com.example.tilingservice.rtree.RTreeSerializer;
import com.example.tilingservice.tile.Tile;
import com.example.tilingservice.tile.TileFactory;
import com.example.tilingservice.utils.GeometryUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Service
@RequiredArgsConstructor
public class TileService {
    private final Parser geoJsonParser;
    private RTree rtree;
        private final RTreeSerializer rtreeSerializer;
    
        @Value("${tiling.error-margin:0.0001}")
        private double errorMargin;
    
        @Value("${tiling.max-recursion-depth:15}")
        private int maxRecursionDepth;
    
    
          @PostConstruct
        public void init() {
            try {
                rtree = rtreeSerializer.deserialize();
            log.info("Loaded R-tree from file storage");
        } catch (IOException e) {
            log.warn("Could not load R-tree from storage, creating new one: {}", e.getMessage());
            rtree = new RTree();
        }
    }
    public List<Tile> generateTiles(String geoJson) {
        Shape shape = geoJsonParser.parse(geoJson);
        List<Tile> tiles = new ArrayList<>();
        
        // Check if we have tiles for this shape in the R-tree first
        List<Tile> existingTiles = rtree.search(shape.getBoundingBox());
        if (!existingTiles.isEmpty()) {
            log.info("Found {} existing tiles for the requested area", existingTiles.size());
            return existingTiles;
        }

        // If no existing tiles, generate new ones
        Tile initialTile = TileFactory.createInitialTile(shape.getBoundingBox());
        processTile(initialTile, shape, tiles, 0);
        
        // Store new tiles in R-tree and persist
        tiles.forEach(rtree::insert);
        try {
            rtreeSerializer.serialize(rtree);
            log.info("Persisted {} new tiles to storage", tiles.size());
        } catch (IOException e) {
            log.error("Failed to persist RTree: {}", e.getMessage());
        }
        
        return tiles;
    }

private void processTile(Tile tile, Shape shape, List<Tile> results, int depth) {
    // Convert degrees to approximate meters for area comparison
    double tileAreaInMeters = GeometryUtils.calculateAreaInMeters(tile.getBoundingBox());
    
    if (depth >= maxRecursionDepth) {
        results.add(tile);
        return;
    }

    // Force subdivision for tiles larger than ~100m²
    if (tileAreaInMeters > 100) {
        for (Tile subtile : tile.subdivide()) {
            processTile(subtile, shape, results, depth + 1);
        }
    } else {
        results.add(tile);
    }
}

    public String generateGeoJson(List<Tile> tiles) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"FeatureCollection\",\"features\":[");
        
        for (int i = 0; i < tiles.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(tiles.get(i).toGeoJson());
        }
        
        sb.append("]}");
        return sb.toString();
    }
    

}
