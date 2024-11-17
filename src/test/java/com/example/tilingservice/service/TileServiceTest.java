package com.example.tilingservice.service;

import com.example.tilingservice.geojson.Parser;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.model.PolygonShape;
import com.example.tilingservice.rtree.RTree;
import com.example.tilingservice.rtree.RTreeSerializer;
import com.example.tilingservice.tile.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TileServiceTest {
    @Mock
    private Parser geoJsonParser;
    
    @Mock
    private RTree rtree;
    
    @Mock
    private RTreeSerializer rTreeSerializer;
    
    @Mock
    private GeometryFactory geometryFactory;
    
    private TileService tileService;

    @BeforeEach
    void setUp() {
        tileService = new TileService(geoJsonParser, rTreeSerializer);
        ReflectionTestUtils.setField(tileService, "geometryFactory", geometryFactory);
    }

    @Test
    void generateTiles_SimplePolygon_ShouldReturnTiles() {
        // Create a simple square polygon
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(1, 1));
        points.add(new Point(1, 0));
        points.add(new Point(0, 0));

        PolygonShape shape = new PolygonShape(points, new ArrayList<>());
        when(geoJsonParser.parse(anyString())).thenReturn(shape);

        // Call with default parameters
        List<Tile> tiles = tileService.generateTiles(
            "dummy-geojson",
            1000.0,  // maxTileArea
            10.0,    // minTileArea
            0.10,    // coverageThreshold
            true     // includeBoundingBox
        );

        assertNotNull(tiles);
        assertFalse(tiles.isEmpty());
    }
}