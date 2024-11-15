package com.example.tilingservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MultiPolygonShapeTest {

    private MultiPolygonShape multiPolygon;
    private PolygonShape polygon1;
    private PolygonShape polygon2;

    @BeforeEach
    void setUp() {
        // Create first polygon (unit square at origin)
        List<Point> points1 = Arrays.asList(
            new Point(0, 0),
            new Point(0, 1),
            new Point(1, 1),
            new Point(1, 0),
            new Point(0, 0)
        );
        polygon1 = new PolygonShape(points1, new ArrayList<>());

        // Create second polygon (unit square at (2,2))
        List<Point> points2 = Arrays.asList(
            new Point(2, 2),
            new Point(2, 3),
            new Point(3, 3),
            new Point(3, 2),
            new Point(2, 2)
        );
        polygon2 = new PolygonShape(points2, new ArrayList<>());

        multiPolygon = new MultiPolygonShape(Arrays.asList(polygon1, polygon2));
    }

    @Test
    void contains_PointInFirstPolygon_ShouldReturnTrue() {
        Point point = new Point(0.5, 0.5);
        assertTrue(multiPolygon.contains(point));
    }

    @Test
    void contains_PointInSecondPolygon_ShouldReturnTrue() {
        Point point = new Point(2.5, 2.5);
        assertTrue(multiPolygon.contains(point));
    }

    @Test
    void contains_PointOutside_ShouldReturnFalse() {
        Point point = new Point(1.5, 1.5);
        assertFalse(multiPolygon.contains(point));
    }

    @Test
    void getArea_TwoUnitSquares_ShouldReturn2() {
        assertEquals(2.0, multiPolygon.getArea(), 0.0001);
    }

    @Test
    void getBoundingBox_ShouldEncloseBothPolygons() {
        BoundingBox box = multiPolygon.getBoundingBox();
        
        assertEquals(0, box.getSouthWest().getLatitude());
        assertEquals(0, box.getSouthWest().getLongitude());
        assertEquals(3, box.getNorthEast().getLatitude());
        assertEquals(3, box.getNorthEast().getLongitude());
    }
}
