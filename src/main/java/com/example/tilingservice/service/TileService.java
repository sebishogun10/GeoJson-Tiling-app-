package com.example.tilingservice.service;

import com.example.tilingservice.geojson.Parser;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.model.Shape;
import com.example.tilingservice.rtree.RTree;
import com.example.tilingservice.tile.Tile;
import com.example.tilingservice.tile.TileFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TileService {
    private final Parser geoJsonParser;
    private final RTree rtree;

    @Value("${tiling.error-margin:0.01}")
    private double errorMargin;

    @Value("${tiling.max-recursion-depth:10}")
    private int maxRecursionDepth;

    public List<Tile> generateTiles(String geoJson) {
        Shape shape = geoJsonParser.parse(geoJson);
        List<Tile> tiles = new ArrayList<>();
        
        Tile initialTile = TileFactory.createInitialTile(shape.getBoundingBox());
        processTile(initialTile, shape, tiles, 0);
        
        // Store tiles in R-tree for future queries
        tiles.forEach(rtree::insert);
        
        return tiles;
    }

    private void processTile(Tile tile, Shape shape, List<Tile> results, int depth) {
        if (depth >= maxRecursionDepth) {
            results.add(tile);
            return;
        }

        boolean allCornersInside = true;
        boolean anyCornersInside = false;

        for (Point corner : tile.getCorners()) {
            boolean isInside = shape.contains(corner);
            allCornersInside &= isInside;
            anyCornersInside |= isInside;
        }

        if (allCornersInside) {
            results.add(tile);
        } else if (anyCornersInside) {
            double tileArea = tile.getArea();
            if (tileArea < errorMargin) {
                results.add(tile);
            } else {
                for (Tile subtile : tile.subdivide()) {
                    processTile(subtile, shape, results, depth + 1);
                }
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
