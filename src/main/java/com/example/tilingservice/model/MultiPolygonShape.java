package com.example.tilingservice.model;

import lombok.Data;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import java.util.List;
import java.util.ArrayList;

@Data
public class MultiPolygonShape implements Shape {
    private List<PolygonShape> polygons;
    private BoundingBox boundingBox;

    public MultiPolygonShape(List<PolygonShape> polygons) {
        this.polygons = polygons;
        this.boundingBox = calculateBoundingBox();
    }

    @Override
    public boolean contains(Point point) {
        if (!boundingBox.contains(point)) {
            return false;
        }
        for (PolygonShape polygon : polygons) {
            if (polygon.contains(point)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getArea() {
        return polygons.stream()
                .mapToDouble(PolygonShape::getArea)
                .sum();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    private BoundingBox calculateBoundingBox() {
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        
        for (PolygonShape polygon : polygons) {
            BoundingBox box = polygon.getBoundingBox();
            minLat = Math.min(minLat, box.getSouthWest().getLatitude());
            maxLat = Math.max(maxLat, box.getNorthEast().getLatitude());
            minLon = Math.min(minLon, box.getSouthWest().getLongitude());
            maxLon = Math.max(maxLon, box.getNorthEast().getLongitude());
        }

        return new BoundingBox(
            new Point(minLat, minLon),
            new Point(maxLat, maxLon)
        );
    }

    @Override
    public Geometry toJtsGeometry(GeometryFactory geometryFactory) {
        try {
            // Convert each polygon to a JTS Polygon
            List<Polygon> jtsPolygons = new ArrayList<>();
            for (PolygonShape polygon : polygons) {
                Geometry geom = polygon.toJtsGeometry(geometryFactory);
                if (geom instanceof Polygon) {
                    jtsPolygons.add((Polygon) geom);
                }
            }

            // Create array of JTS Polygons
            Polygon[] polygonArray = jtsPolygons.toArray(new Polygon[0]);

            // Create and return MultiPolygon
            return geometryFactory.createMultiPolygon(polygonArray);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert MultiPolygonShape to JTS Geometry: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the list of constituent polygons
     * @return List of PolygonShape objects
     */
    public List<PolygonShape> getPolygons() {
        return polygons;
    }

    /**
     * Checks if this MultiPolygonShape has any polygons
     * @return true if there are one or more polygons, false otherwise
     */
    public boolean hasPolygons() {
        return polygons != null && !polygons.isEmpty();
    }

    /**
     * Gets the total number of holes across all polygons
     * @return the total number of holes
     */
    public int getTotalHolesCount() {
        return polygons.stream()
                .filter(p -> p.hasHoles())
                .mapToInt(p -> p.getHoles().size())
                .sum();
    }
}