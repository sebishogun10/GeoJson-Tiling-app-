package com.example.tilingservice.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoundingBox {
    private Point southWest;
    private Point northEast;

    public boolean contains(Point point) {
        return point.getLatitude() >= southWest.getLatitude() &&
               point.getLatitude() <= northEast.getLatitude() &&
               point.getLongitude() >= southWest.getLongitude() &&
               point.getLongitude() <= northEast.getLongitude();
    }

    public boolean intersects(BoundingBox other) {
        return !(other.getNorthEast().getLongitude() < this.getSouthWest().getLongitude() ||
                other.getSouthWest().getLongitude() > this.getNorthEast().getLongitude() ||
                other.getNorthEast().getLatitude() < this.getSouthWest().getLatitude() ||
                other.getSouthWest().getLatitude() > this.getNorthEast().getLatitude());
    }

    public BoundingBox union(BoundingBox other) {
        double minLat = Math.min(this.southWest.getLatitude(), other.getSouthWest().getLatitude());
        double minLon = Math.min(this.southWest.getLongitude(), other.getSouthWest().getLongitude());
        double maxLat = Math.max(this.northEast.getLatitude(), other.getNorthEast().getLatitude());
        double maxLon = Math.max(this.northEast.getLongitude(), other.getNorthEast().getLongitude());

        return new BoundingBox(
            new Point(minLat, minLon),
            new Point(maxLat, maxLon)
        );
    }


}
