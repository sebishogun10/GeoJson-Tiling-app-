package com.example.tilingservice.model;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public interface Shape {
    boolean contains(Point point);
    BoundingBox getBoundingBox();
    double getArea();
    Geometry toJtsGeometry(GeometryFactory geometryFactory);
}
