package com.example.tilingservice.tile;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;

public class TileFactory {
    public static RectangleTile createInitialTile(BoundingBox boundingBox) {
        return new RectangleTile(boundingBox);
    }

    public static RectangleTile createTile(Point southWest, Point northEast) {
        return new RectangleTile(new BoundingBox(southWest, northEast));
    }
}
