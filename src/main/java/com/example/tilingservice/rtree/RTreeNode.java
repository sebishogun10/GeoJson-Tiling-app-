package com.example.tilingservice.rtree;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.tile.Tile;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class RTreeNode {
    private String id = UUID.randomUUID().toString();
    private BoundingBox boundingBox;
    private List<RTreeNode> children; // For internal nodes
    private List<Tile> tiles;         // For leaf nodes
    private boolean isLeaf;
    private static final int MAX_ENTRIES = 32;

    public RTreeNode() {
        this.children = new ArrayList<>();
        this.tiles = new ArrayList<>();
        this.isLeaf = true; // Initially, the node is a leaf
        this.boundingBox = null; // Will be updated when entries are added
    }


    public void insert(Tile tile) {
        if (isLeaf) {
            if (tiles.size() < MAX_ENTRIES) {
                tiles.add(tile);
                updateBoundingBox();
            } else {
                
                splitLeafNode();
                // After splitting, insert the tile into the appropriate child
                RTreeNode bestChild = chooseBestChild(tile.getBoundingBox());
                bestChild.insert(tile);
                updateBoundingBox();
            }
        } else {
            // Internal node
            RTreeNode bestChild = chooseBestChild(tile.getBoundingBox());
            bestChild.insert(tile);
            updateBoundingBox();
        }
    }

   
    public List<Tile> search(BoundingBox searchBox) {
        List<Tile> results = new ArrayList<>();

        if (boundingBox == null || !boundingBox.intersects(searchBox)) {
            return results; // No intersection; skip this node
        }

        if (isLeaf) {
            // Leaf node: check tiles
            for (Tile tile : tiles) {
                if (tile.getBoundingBox().intersects(searchBox)) {
                    results.add(tile);
                }
            }
        } else {
            // Internal node: recurse into children
            for (RTreeNode child : children) {
                results.addAll(child.search(searchBox));
            }
        }

        return results;
    }

  
    private void updateBoundingBox() {
        if (isLeaf) {
            // Leaf node: calculate bounding box from tiles
            if (tiles.isEmpty()) {
                boundingBox = null;
                return;
            }
            boundingBox = tiles.get(0).getBoundingBox();
            for (Tile tile : tiles) {
                boundingBox = boundingBox.union(tile.getBoundingBox());
            }
        } else {
            // Internal node: calculate bounding box from children
            if (children.isEmpty()) {
                boundingBox = null;
                return;
            }
            boundingBox = children.get(0).getBoundingBox();
            for (RTreeNode child : children) {
                boundingBox = boundingBox.union(child.getBoundingBox());
            }
        }
    }


    private RTreeNode chooseBestChild(BoundingBox box) {
        RTreeNode bestNode = null;
        double minIncrease = Double.MAX_VALUE;

        for (RTreeNode child : children) {
            double increase = calculateBoundingBoxIncrease(child.getBoundingBox(), box);
            if (increase < minIncrease) {
                minIncrease = increase;
                bestNode = child;
            }
        }

        return bestNode != null ? bestNode : children.get(0);
    }


    private double calculateBoundingBoxIncrease(BoundingBox current, BoundingBox newBox) {
        if (current == null) return calculateArea(newBox);

        double currentArea = calculateArea(current);
        BoundingBox combinedBox = current.union(newBox);
        double enlargedArea = calculateArea(combinedBox);

        return enlargedArea - currentArea;
    }


    private double calculateArea(BoundingBox box) {
        if (box == null) return 0;
        double latDiff = box.getNorthEast().getLatitude() - box.getSouthWest().getLatitude();
        double lonDiff = box.getNorthEast().getLongitude() - box.getSouthWest().getLongitude();
        return Math.abs(latDiff * lonDiff);
    }

    private void splitLeafNode() {
        
        RTreeNode leaf1 = new RTreeNode();
        RTreeNode leaf2 = new RTreeNode();
        leaf1.setLeaf(true);
        leaf2.setLeaf(true);

        
        Tile seed1 = null, seed2 = null;
        double maxDistance = -1;

        for (int i = 0; i < tiles.size(); i++) {
            for (int j = i + 1; j < tiles.size(); j++) {
                double distance = calculateDistance(
                        tiles.get(i).getBoundingBox(),
                        tiles.get(j).getBoundingBox()
                );
                if (distance > maxDistance) {
                    maxDistance = distance;
                    seed1 = tiles.get(i);
                    seed2 = tiles.get(j);
                }
            }
        }

        
        leaf1.getTiles().add(seed1);
        leaf2.getTiles().add(seed2);

        
        tiles.remove(seed1);
        tiles.remove(seed2);

        
        for (Tile tile : tiles) {
            double increaseLeaf1 = calculateBoundingBoxIncrease(leaf1.getBoundingBox(), tile.getBoundingBox());
            double increaseLeaf2 = calculateBoundingBoxIncrease(leaf2.getBoundingBox(), tile.getBoundingBox());

            if (increaseLeaf1 < increaseLeaf2) {
                leaf1.getTiles().add(tile);
            } else {
                leaf2.getTiles().add(tile);
            }
        }

       
        tiles.clear();

        // Convert current node to an internal node
        isLeaf = false;
        children = new ArrayList<>();
        children.add(leaf1);
        children.add(leaf2);

        // Update bounding boxes
        leaf1.updateBoundingBox();
        leaf2.updateBoundingBox();
        updateBoundingBox();
    }

  
    private double calculateDistance(BoundingBox box1, BoundingBox box2) {
        Point center1 = calculateCenter(box1);
        Point center2 = calculateCenter(box2);

        double latDiff = center1.getLatitude() - center2.getLatitude();
        double lonDiff = center1.getLongitude() - center2.getLongitude();

        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }


    private Point calculateCenter(BoundingBox box) {
        return new Point(
                (box.getSouthWest().getLatitude() + box.getNorthEast().getLatitude()) / 2,
                (box.getSouthWest().getLongitude() + box.getNorthEast().getLongitude()) / 2
        );
    }

  
    private void splitInternalNode() {
        
        RTreeNode node1 = new RTreeNode();
        RTreeNode node2 = new RTreeNode();
        node1.setLeaf(false);
        node2.setLeaf(false);

                RTreeNode seed1 = null, seed2 = null;
        double maxDistance = -1;

        for (int i = 0; i < children.size(); i++) {
            for (int j = i + 1; j < children.size(); j++) {
                double distance = calculateDistance(
                        children.get(i).getBoundingBox(),
                        children.get(j).getBoundingBox()
                );
                if (distance > maxDistance) {
                    maxDistance = distance;
                    seed1 = children.get(i);
                    seed2 = children.get(j);
                }
            }
        }

        
        node1.getChildren().add(seed1);
        node2.getChildren().add(seed2);

        
        children.remove(seed1);
        children.remove(seed2);

        
        for (RTreeNode child : children) {
            double increaseNode1 = calculateBoundingBoxIncrease(node1.getBoundingBox(), child.getBoundingBox());
            double increaseNode2 = calculateBoundingBoxIncrease(node2.getBoundingBox(), child.getBoundingBox());

            if (increaseNode1 < increaseNode2) {
                node1.getChildren().add(child);
            } else {
                node2.getChildren().add(child);
            }
        }

 
        children.clear();

 
        children.add(node1);
        children.add(node2);

 
        node1.updateBoundingBox();
        node2.updateBoundingBox();
        updateBoundingBox();
    }
}
