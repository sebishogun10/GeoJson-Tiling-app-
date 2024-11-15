package com.example.tilingservice.geojson;

import com.example.tilingservice.model.MultiPolygonShape;
import com.example.tilingservice.model.PolygonShape;
import com.example.tilingservice.model.Shape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoJsonParserTest {

    private GeoJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new GeoJsonParser();
    }

    @Test
    void parse_ValidPolygon_ShouldReturnPolygonShape() {
        String geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [[0,0], [0,1], [1,1], [1,0], [0,0]]
                    ]
                }
            }
            """;

        Shape shape = parser.parse(geoJson);

        assertNotNull(shape);
        assertTrue(shape instanceof PolygonShape);
    }

    @Test
    void parse_ValidMultiPolygon_ShouldReturnMultiPolygonShape() {
        String geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "MultiPolygon",
                    "coordinates": [
                        [[[0,0], [0,1], [1,1], [1,0], [0,0]]],
                        [[[2,2], [2,3], [3,3], [3,2], [2,2]]]
                    ]
                }
            }
            """;

        Shape shape = parser.parse(geoJson);

        assertNotNull(shape);
        assertTrue(shape instanceof MultiPolygonShape);
    }

    @Test
    void parse_InvalidGeoJson_ShouldThrowException() {
        String invalidGeoJson = "{ invalid json }";
        
        assertThrows(IllegalArgumentException.class, () -> parser.parse(invalidGeoJson));
    }
}
