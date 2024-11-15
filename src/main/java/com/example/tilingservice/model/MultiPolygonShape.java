package com.example.tilingservice.model;

import lombok.Data;
import java.util.List;

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
}
