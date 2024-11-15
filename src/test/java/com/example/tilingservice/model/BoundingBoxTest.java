package com.example.tilingservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoundingBoxTest {

    private BoundingBox boundingBox;
    private Point southWest;
    private Point northEast;

    @BeforeEach
    void setUp() {
        southWest = new Point(0, 0);
        northEast = new Point(1, 1);
        boundingBox = new BoundingBox(southWest, northEast);
    }

    @Test
    void contains_PointInside_ShouldReturnTrue() {
        Point point = new Point(0.5, 0.5);
        assertTrue(boundingBox.contains(point));
    }

    @Test
    void contains_PointOutside_ShouldReturnFalse() {
        Point point = new Point(2, 2);
        assertFalse(boundingBox.contains(point));
    }

    @Test
    void intersects_OverlappingBoxes_ShouldReturnTrue() {
        BoundingBox other = new BoundingBox(
            new Point(0.5, 0.5),
            new Point(1.5, 1.5)
        );
        assertTrue(boundingBox.intersects(other));
    }

    @Test
    void intersects_NonOverlappingBoxes_ShouldReturnFalse() {
        BoundingBox other = new BoundingBox(
            new Point(2, 2),
            new Point(3, 3)
        );
        assertFalse(boundingBox.intersects(other));
    }
}
