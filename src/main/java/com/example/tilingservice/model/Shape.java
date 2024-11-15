package com.example.tilingservice.model;

public interface Shape {
    boolean contains(Point point);
    BoundingBox getBoundingBox();
    double getArea();
}
