package com.example.tilingservice.rtree;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.tile.Tile;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.util.List;

// @Component
public class RTree {
    @Getter
    private RTreeNode root;

    public RTree() {
        this.root = new RTreeNode();
    }

    public void insert(Tile tile) {
        root.insert(tile);
    }

    public List<Tile> search(BoundingBox searchBox) {
        return root.search(searchBox);
    }
}
