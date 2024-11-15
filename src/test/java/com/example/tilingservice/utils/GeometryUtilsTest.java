package com.example.tilingservice.utils;

import com.example.tilingservice.model.Point;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeometryUtilsTest {

    @Test
    void pointInPolygon_PointInside_ShouldReturnTrue() {
        List<Point> polygon = Arrays.asList(
            new Point(0, 0),
            new Point(0, 2),
            new Point(2, 2),
            new Point(2, 0),
            new Point(0, 0)
        );
        Point point = new Point(1, 1);

        assertTrue(GeometryUtils.pointInPolygon(point, polygon));
    }

    @Test
    void pointInPolygon_PointOutside_ShouldReturnFalse() {
        List<Point> polygon = Arrays.asList(
            new Point(0, 0),
            new Point(0, 2),
            new Point(2, 2),
            new Point(2, 0),
            new Point(0, 0)
        );
        Point point = new Point(3, 3);

        assertFalse(GeometryUtils.pointInPolygon(point, polygon));
    }

    @Test
    void calculatePolygonArea_SimpleSquare_ShouldReturnCorrectArea() {
        List<Point> polygon = Arrays.asList(
            new Point(0, 0),
            new Point(0, 1),
            new Point(1, 1),
            new Point(1, 0),
            new Point(0, 0)
        );

        double area = GeometryUtils.calculatePolygonArea(polygon);
        assertEquals(1.0, area, 0.0001);
    }

    @Test
    void calculateDistance_KnownPoints_ShouldReturnCorrectDistance() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(1, 1);

        double distance = GeometryUtils.calculateDistance(p1, p2);
        assertTrue(distance > 0);
    }
}
