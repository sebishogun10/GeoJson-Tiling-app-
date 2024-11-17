package com.example.tilingservice.service;

import com.example.tilingservice.geojson.Parser;
import com.example.tilingservice.model.Shape;
import com.example.tilingservice.rtree.RTree;
import com.example.tilingservice.rtree.RTreeSerializer;
import com.example.tilingservice.tile.Tile;
import com.example.tilingservice.tile.TileFactory;
import com.example.tilingservice.utils.GeometryUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TileService {
    private final Parser geoJsonParser;
    private RTree rtree;
    private final RTreeSerializer rtreeSerializer;

    @Autowired
    private GeometryFactory geometryFactory;

    @Value("${tiling.error-margin:0.0001}")
    private double errorMargin;

    @Value("${tiling.max-recursion-depth:15}")
    private int maxRecursionDepth;

    // Adjusted constants for better tiling
    private static final double MAX_TILE_AREA = 1000.0; // Maximum area before subdivision (in square meters)
    private static final double MIN_TILE_AREA = 10.0;   // Minimum area for tiles (in square meters)
    private static final double COVERAGE_THRESHOLD = 0.10;  // Lower threshold for better coverage
    private static final boolean INCLUDE_BOUNDING_BOX = true; // Include the overall bounding box

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

        // Add the bounding box tile if configured
        Tile boundingBoxTile = TileFactory.createInitialTile(shape.getBoundingBox());
        if (INCLUDE_BOUNDING_BOX) {
            tiles.add(boundingBoxTile);
        }

        // Check for existing tiles in the R-tree
        List<Tile> existingTiles = rtree.search(shape.getBoundingBox());
        if (!existingTiles.isEmpty()) {
            log.info("Found {} existing tiles for the requested area", existingTiles.size());
            tiles.addAll(existingTiles);
            return tiles;
        }

        // Initial subdivision if the area is too large
        double initialAreaInMeters = GeometryUtils.calculateAreaInMeters(boundingBoxTile.getBoundingBox());
        log.info("Initial area in square meters: {}", initialAreaInMeters);

        if (initialAreaInMeters > MAX_TILE_AREA) {
            // Force initial subdivision
            List<Tile> initialTiles = new ArrayList<>(boundingBoxTile.subdivide());
            for (Tile subtile : initialTiles) {
                processTile(subtile, shape, tiles, 1);
            }
        } else {
            processTile(boundingBoxTile, shape, tiles, 0);
        }

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
        // Log processing information
        double tileAreaInMeters = GeometryUtils.calculateAreaInMeters(tile.getBoundingBox());
        log.debug("Processing tile at depth {} with area {} sq meters", depth, tileAreaInMeters);

        // Quick check with bounding boxes
        if (!tile.getBoundingBox().intersects(shape.getBoundingBox())) {
            log.debug("Tile rejected - no intersection with shape bounding box");
            return;
        }

        try {
            // Convert to JTS geometries for precise intersection checking
            Geometry tileGeometry = tile.toJtsPolygon(geometryFactory);
            Geometry shapeGeometry = shape.toJtsGeometry(geometryFactory);
            IntersectionMatrix matrix = tileGeometry.relate(shapeGeometry);

            if (matrix.isDisjoint()) {
                log.debug("Tile rejected - disjoint with shape");
                return;
            }

            // Calculate intersection metrics
            Geometry intersection = tileGeometry.intersection(shapeGeometry);
            double intersectionArea = intersection.getArea();
            double tileArea = tileGeometry.getArea();
            double coverageRatio = intersectionArea / tileArea;
            
            log.debug("Tile coverage ratio: {}", coverageRatio);

            // Subdivision logic
            if (tileAreaInMeters > MAX_TILE_AREA) {
                // Always subdivide if tile is too large
                log.debug("Subdividing tile - above maximum area");
                for (Tile subtile : tile.subdivide()) {
                    processTile(subtile, shape, results, depth + 1);
                }
            } else if (depth >= maxRecursionDepth || tileAreaInMeters <= MIN_TILE_AREA) {
                // Add tile if we've reached limits and have sufficient coverage
                if (coverageRatio >= COVERAGE_THRESHOLD) {
                    log.debug("Adding tile - reached depth/area limit with sufficient coverage");
                    results.add(tile);
                }
            } else if (matrix.isContains() || coverageRatio > 0.95) {
                // Add tile if it's fully contained or has high coverage
                log.debug("Adding tile - high coverage or contained");
                results.add(tile);
            } else {
                // Subdivide for better granularity
                log.debug("Subdividing tile - for better granularity");
                for (Tile subtile : tile.subdivide()) {
                    processTile(subtile, shape, results, depth + 1);
                }
            }

        } catch (Exception e) {
            log.warn("Error processing tile intersection: {}", e.getMessage());
            // Fallback behavior based on area
            if (tileAreaInMeters > MAX_TILE_AREA && depth < maxRecursionDepth) {
                for (Tile subtile : tile.subdivide()) {
                    processTile(subtile, shape, results, depth + 1);
                }
            } else if (tileAreaInMeters <= MAX_TILE_AREA) {
                results.add(tile);
            }
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