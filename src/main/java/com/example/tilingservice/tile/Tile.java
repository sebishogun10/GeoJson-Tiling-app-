package com.example.tilingservice.tile;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import java.util.List;

public interface Tile {
    BoundingBox getBoundingBox();
    List<Point> getCorners();
    double getArea();
    List<? extends Tile> subdivide();
    String toGeoJson();
}
