package com.example.tilingservice.model;

import lombok.Data;
import com.example.tilingservice.utils.GeometryUtils;
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
}
