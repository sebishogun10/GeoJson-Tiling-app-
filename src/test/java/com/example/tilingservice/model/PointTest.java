package com.example.tilingservice.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    @Test
    void constructor_WithCoordinatesArray_ShouldSetCorrectly() {
        double[] coordinates = {10.5, 20.7};
        Point point = new Point(coordinates);
        
        assertEquals(20.7, point.getLatitude());
        assertEquals(10.5, point.getLongitude());
    }

    @Test
    void toArray_ShouldReturnCorrectArray() {
        Point point = new Point(20.7, 10.5);
        double[] coordinates = point.toArray();
        
        assertEquals(10.5, coordinates[0]); // longitude
        assertEquals(20.7, coordinates[1]); // latitude
    }
}
