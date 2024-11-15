package com.example.tilingservice.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point {
    private double latitude;
    private double longitude;

    public Point(double[] coordinates) {
        this.longitude = coordinates[0];
        this.latitude = coordinates[1];
    }

    public double[] toArray() {
        return new double[]{longitude, latitude};
    }
}
