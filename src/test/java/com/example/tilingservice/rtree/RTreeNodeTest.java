package com.example.tilingservice.rtree;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.tile.RectangleTile;
import com.example.tilingservice.tile.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RTreeNodeTest {

    private RTreeNode node;

    @BeforeEach
    void setUp() {
        node = new RTreeNode();
    }


    @Test
    void search_ExistingTile_ShouldBeFound() {
        BoundingBox box = new BoundingBox(
            new Point(0, 0),
            new Point(1, 1)
        );
        Tile tile = new RectangleTile(box);
        node.insert(tile);

        List<Tile> found = node.search(box);

        assertEquals(1, found.size());
        assertEquals(box, found.get(0).getBoundingBox());
    }

    @Test
    void search_NonExistentArea_ShouldReturnEmpty() {
        BoundingBox box1 = new BoundingBox(
            new Point(0, 0),
            new Point(1, 1)
        );
        node.insert(new RectangleTile(box1));

        BoundingBox searchBox = new BoundingBox(
            new Point(2, 2),
            new Point(3, 3)
        );
        List<Tile> found = node.search(searchBox);

        assertTrue(found.isEmpty());
    }
}
