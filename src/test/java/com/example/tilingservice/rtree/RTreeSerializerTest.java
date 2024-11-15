package com.example.tilingservice.rtree;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.tile.RectangleTile;
import com.example.tilingservice.tile.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RTreeSerializerTest {

    private RTreeSerializer serializer;
    private RTree rtree;

    @BeforeEach
    void setUp() {
        serializer = new RTreeSerializer();
        rtree = new RTree();
    }

    @Test
    void serializeAndDeserialize_ShouldMaintainTreeStructure() throws IOException {
        // Create and insert test tile
        BoundingBox box = new BoundingBox(
            new Point(0, 0),
            new Point(1, 1)
        );
        Tile tile = new RectangleTile(box);
        rtree.insert(tile);

        // Serialize and then deserialize
        serializer.serialize(rtree);
        RTree deserializedTree = serializer.deserialize();

        // Verify structure is maintained
        List<Tile> originalResults = rtree.search(box);
        List<Tile> deserializedResults = deserializedTree.search(box);

        assertEquals(originalResults.size(), deserializedResults.size());
        assertEquals(
            originalResults.get(0).getBoundingBox(),
            deserializedResults.get(0).getBoundingBox()
        );
    }

    @Test
    void deserialize_NonexistentFile_ShouldReturnNewTree() throws IOException {
        RTree result = serializer.deserialize();
        assertNotNull(result);
        assertTrue(result.search(new BoundingBox(
            new Point(0, 0),
            new Point(1, 1)
        )).isEmpty());
    }
}
