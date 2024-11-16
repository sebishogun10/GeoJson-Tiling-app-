package com.example.tilingservice.model;

import lombok.Data;
import com.example.tilingservice.utils.GeometryUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import java.util.List;
import java.util.ArrayList;

@Data
public class PolygonShape implements Shape {
    private List<Point> outerBoundary;
    private List<List<Point>> holes;
    private BoundingBox boundingBox;

    public PolygonShape(List<Point> outerBoundary, List<List<Point>> holes) {
        this.outerBoundary = outerBoundary;
        this.holes = holes != null ? holes : new ArrayList<>();
        this.boundingBox = calculateBoundingBox();
    }

    @Override
    public boolean contains(Point point) {
        if (!boundingBox.contains(point)) {
            return false;
        }

        boolean inPolygon = GeometryUtils.pointInPolygon(point, outerBoundary);
        
        if (!inPolygon) {
            return false;
        }

        // Check if point is in any hole
        for (List<Point> hole : holes) {
            if (GeometryUtils.pointInPolygon(point, hole)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public double getArea() {
        double area = GeometryUtils.calculatePolygonArea(outerBoundary);
        for (List<Point> hole : holes) {
            area -= GeometryUtils.calculatePolygonArea(hole);
        }
        return area;
    }

    private BoundingBox calculateBoundingBox() {
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (Point p : outerBoundary) {
            minLat = Math.min(minLat, p.getLatitude());
            maxLat = Math.max(maxLat, p.getLatitude());
            minLon = Math.min(minLon, p.getLongitude());
            maxLon = Math.max(maxLon, p.getLongitude());
        }

        return new BoundingBox(
            new Point(minLat, minLon),
            new Point(maxLat, maxLon)
        );
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Checks if this polygon has any holes
     * @return true if the polygon has one or more holes, false otherwise
     */
    public boolean hasHoles() {
        return holes != null && !holes.isEmpty();
    }

    /**
     * Gets the list of holes in this polygon
     * @return List of holes, where each hole is a list of points
     */
    public List<List<Point>> getHoles() {
        return holes;
    }

    /**
     * Gets the outer boundary points of the polygon
     * @return List of points forming the outer boundary
     */
    public List<Point> getOuterBoundary() {
        return outerBoundary;
    }

    @Override
    public Geometry toJtsGeometry(GeometryFactory geometryFactory) {
        // Convert outer boundary to coordinates
        Coordinate[] coordinates = new Coordinate[outerBoundary.size() + 1];
        for (int i = 0; i < outerBoundary.size(); i++) {
            Point point = outerBoundary.get(i);
            coordinates[i] = new Coordinate(point.getLongitude(), point.getLatitude());
        }
        // Close the ring
        coordinates[outerBoundary.size()] = coordinates[0];
        
        // Create the polygon ring
        LinearRing ring = geometryFactory.createLinearRing(coordinates);
        
        // Handle holes
        LinearRing[] holes = null;
        if (hasHoles()) {
            holes = new LinearRing[this.holes.size()];
            
            for (int i = 0; i < this.holes.size(); i++) {
                List<Point> hole = this.holes.get(i);
                Coordinate[] holeCoords = new Coordinate[hole.size() + 1];
                
                for (int j = 0; j < hole.size(); j++) {
                    Point point = hole.get(j);
                    holeCoords[j] = new Coordinate(point.getLongitude(), point.getLatitude());
                }
                holeCoords[hole.size()] = holeCoords[0]; // Close the hole ring
                
                holes[i] = geometryFactory.createLinearRing(holeCoords);
            }
        }
        
        return geometryFactory.createPolygon(ring, holes);
    }
}