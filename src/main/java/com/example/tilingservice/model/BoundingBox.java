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
}
