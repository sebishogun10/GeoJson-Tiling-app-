package com.example.tilingservice.rtree;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.tile.RectangleTile;
import com.example.tilingservice.tile.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RTreeTest {

    private RTree rtree;

    @BeforeEach
    void setUp() {
        rtree = new RTree();
    }

    @Test
    void insert_SingleTile_ShouldBeRetrievable() {
        BoundingBox box = new BoundingBox(
            new Point(0, 0),
            new Point(1, 1)
        );
        Tile tile = new RectangleTile(box);

        rtree.insert(tile);

        List<Tile> found = rtree.search(box);

        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        assertEquals(tile.getBoundingBox(), found.get(0).getBoundingBox());
    }

    @Test
    void search_NonIntersectingBox_ShouldReturnEmpty() {
        BoundingBox box1 = new BoundingBox(
            new Point(0, 0),
            new Point(1, 1)
        );
        rtree.insert(new RectangleTile(box1));

        BoundingBox searchBox = new BoundingBox(
            new Point(2, 2),
            new Point(3, 3)
        );
        List<Tile> found = rtree.search(searchBox);

        assertTrue(found.isEmpty());
    }

    @Test
    void insert_MultipleTiles_ShouldHandleCorrectly() {
        for (int i = 0; i < 5; i++) {
            BoundingBox box = new BoundingBox(
                new Point(i, i),
                new Point(i + 1, i + 1)
            );
            rtree.insert(new RectangleTile(box));
        }

        BoundingBox searchBox = new BoundingBox(
            new Point(0, 0),
            new Point(3, 3)
        );
        List<Tile> found = rtree.search(searchBox);

        assertFalse(found.isEmpty());
        assertTrue(found.size() >= 3);
    }
}
