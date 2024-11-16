package com.example.tilingservice.tile;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public interface Tile {
    BoundingBox getBoundingBox();
    List<Point> getCorners();
    double getArea();
    List<? extends Tile> subdivide();
    String toGeoJson();
    Polygon toJtsPolygon(GeometryFactory geometryFactory);

    default Polygon createJtsPolygon(GeometryFactory geometryFactory) {
        List<Point> corners = getCorners();
        org.locationtech.jts.geom.Coordinate[] coordinates = new org.locationtech.jts.geom.Coordinate[corners.size() + 1];
        
        // Convert corners to JTS coordinates
        for (int i = 0; i < corners.size(); i++) {
            Point corner = corners.get(i);
            coordinates[i] = new org.locationtech.jts.geom.Coordinate(
                corner.getLongitude(), 
                corner.getLatitude()
            );
        }
        
        // Close the ring by adding the first point again
        coordinates[corners.size()] = coordinates[0];
        
        // Create and return the polygon
        org.locationtech.jts.geom.LinearRing ring = geometryFactory.createLinearRing(coordinates);
        return geometryFactory.createPolygon(ring);
    }
}
