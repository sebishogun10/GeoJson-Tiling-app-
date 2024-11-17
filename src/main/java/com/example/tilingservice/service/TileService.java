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

    private double maxTileArea;
    private double minTileArea;
    private double coverageThreshold;
    private boolean includeBoundingBox;

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

    public List<Tile> generateTiles(String geoJson, double maxTileArea, double minTileArea, 
                                  double coverageThreshold, boolean includeBoundingBox) {
        this.maxTileArea = maxTileArea;
        this.minTileArea = minTileArea;
        this.coverageThreshold = coverageThreshold;
        this.includeBoundingBox = includeBoundingBox;
        
        Shape shape = geoJsonParser.parse(geoJson);
        List<Tile> tiles = new ArrayList<>();

        if (includeBoundingBox) {
            Tile boundingBoxTile = TileFactory.createInitialTile(shape.getBoundingBox());
            tiles.add(boundingBoxTile);
        }

        List<Tile> existingTiles = rtree.search(shape.getBoundingBox());
        if (!existingTiles.isEmpty()) {
            log.info("Found {} existing tiles for the requested area", existingTiles.size());
            tiles.addAll(existingTiles);
            return tiles;
        }

        Tile initialTile = TileFactory.createInitialTile(shape.getBoundingBox());
        double initialAreaInMeters = GeometryUtils.calculateAreaInMeters(initialTile.getBoundingBox());
        log.info("Initial area in square meters: {}", initialAreaInMeters);

        if (initialAreaInMeters > maxTileArea) {
            List<Tile> initialTiles = new ArrayList<>(initialTile.subdivide());
            for (Tile subtile : initialTiles) {
                processTile(subtile, shape, tiles, 1);
            }
        } else {
            processTile(initialTile, shape, tiles, 0);
        }

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
        double tileAreaInMeters = GeometryUtils.calculateAreaInMeters(tile.getBoundingBox());

        if (!tile.getBoundingBox().intersects(shape.getBoundingBox())) {
            return;
        }

        try {
            Geometry tileGeometry = tile.toJtsPolygon(geometryFactory);
            Geometry shapeGeometry = shape.toJtsGeometry(geometryFactory);
            IntersectionMatrix matrix = tileGeometry.relate(shapeGeometry);

            if (matrix.isDisjoint()) {
                return;
            }

            Geometry intersection = tileGeometry.intersection(shapeGeometry);
            double intersectionArea = intersection.getArea();
            double tileArea = tileGeometry.getArea();
            double coverageRatio = intersectionArea / tileArea;

            if (tileAreaInMeters > maxTileArea && depth < 15) {
                for (Tile subtile : tile.subdivide()) {
                    processTile(subtile, shape, results, depth + 1);
                }
            } else if (depth >= 15 || tileAreaInMeters <= minTileArea) {
                if (coverageRatio >= coverageThreshold) {
                    results.add(tile);
                }
            } else if (matrix.isContains() || coverageRatio > 0.95) {
                results.add(tile);
            } else {
                for (Tile subtile : tile.subdivide()) {
                    processTile(subtile, shape, results, depth + 1);
                }
            }

        } catch (Exception e) {
            log.warn("Error processing tile intersection: {}", e.getMessage());
            if (tileAreaInMeters > maxTileArea && depth < 15) {
                for (Tile subtile : tile.subdivide()) {
                    processTile(subtile, shape, results, depth + 1);
                }
            } else if (tileAreaInMeters <= maxTileArea) {
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
