package com.example.tilingservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PolygonShapeTest {

    private PolygonShape polygon;
    private List<Point> outerBoundary;

    @BeforeEach
    void setUp() {
        outerBoundary = Arrays.asList(
            new Point(0, 0),
            new Point(0, 1),
            new Point(1, 1),
            new Point(1, 0),
            new Point(0, 0)
        );
        polygon = new PolygonShape(outerBoundary, new ArrayList<>());
    }

    @Test
    void contains_PointInside_ShouldReturnTrue() {
        Point point = new Point(0.5, 0.5);
        assertTrue(polygon.contains(point));
    }

    @Test
    void contains_PointOutside_ShouldReturnFalse() {
        Point point = new Point(2, 2);
        assertFalse(polygon.contains(point));
    }

    @Test
    void contains_PointInHole_ShouldReturnFalse() {
        List<Point> hole = Arrays.asList(
            new Point(0.25, 0.25),
            new Point(0.25, 0.75),
            new Point(0.75, 0.75),
            new Point(0.75, 0.25),
            new Point(0.25, 0.25)
        );
        polygon = new PolygonShape(outerBoundary, Arrays.asList(hole));
        
        Point point = new Point(0.5, 0.5);
        assertFalse(polygon.contains(point));
    }

    @Test
    void getArea_SimpleSquare_ShouldReturnCorrectArea() {
        assertEquals(1.0, polygon.getArea(), 0.0001);
    }
}
