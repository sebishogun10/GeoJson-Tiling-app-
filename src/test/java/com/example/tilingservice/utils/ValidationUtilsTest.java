package com.example.tilingservice.utils;

import com.example.tilingservice.model.Point;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void isValidPolygon_ValidPolygon_ShouldReturnTrue() {
        List<Point> polygon = Arrays.asList(
            new Point(0, 0),
            new Point(0, 1),
            new Point(1, 1),
            new Point(1, 0),
            new Point(0, 0)
        );

        assertTrue(ValidationUtils.isValidPolygon(polygon));
    }

    @Test
    void isValidPolygon_NotClosed_ShouldReturnFalse() {
        List<Point> polygon = Arrays.asList(
            new Point(0, 0),
            new Point(0, 1),
            new Point(1, 1),
            new Point(1, 0)
        );

        assertFalse(ValidationUtils.isValidPolygon(polygon));
    }

    @Test
    void isValidPolygon_TooFewPoints_ShouldReturnFalse() {
        List<Point> polygon = Arrays.asList(
            new Point(0, 0),
            new Point(1, 1)
        );

        assertFalse(ValidationUtils.isValidPolygon(polygon));
    }

    @Test
    void isValidPolygon_SelfIntersecting_ShouldReturnFalse() {
        List<Point> polygon = Arrays.asList(
            new Point(0, 0),
            new Point(2, 2),
            new Point(2, 0),
            new Point(0, 2),
            new Point(0, 0)
        );

        assertFalse(ValidationUtils.isValidPolygon(polygon));
    }
}
