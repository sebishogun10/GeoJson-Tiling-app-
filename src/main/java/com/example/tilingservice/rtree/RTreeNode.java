package com.example.tilingservice.rtree;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import com.example.tilingservice.tile.Tile;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RTreeNode {
    private String id = java.util.UUID.randomUUID().toString();
    private BoundingBox boundingBox;
    private List<RTreeNode> children;
    private Tile tile;
    private boolean isLeaf;
    private static final int MAX_ENTRIES = 4;

    public RTreeNode() {
        this.children = new ArrayList<>();
        this.isLeaf = true;
        this.boundingBox = new BoundingBox(new Point(0, 0), new Point(0, 0));
    }

    public void insert(Tile tile) {
        if (isLeaf) {
            if (children.size() < MAX_ENTRIES) {
                RTreeNode node = new RTreeNode();
                node.setTile(tile);
                node.setBoundingBox(tile.getBoundingBox());
                children.add(node);
                updateBoundingBox();
            } else {
                List<RTreeNode> oldChildren = new ArrayList<>(children);
                isLeaf = false;
                children.clear();
                
                // Create two new leaf nodes
                RTreeNode node1 = new RTreeNode();
                RTreeNode node2 = new RTreeNode();
                children.add(node1);
                children.add(node2);
                
                // Redistribute old children
                for (RTreeNode child : oldChildren) {
                    if (node1.children.size() < MAX_ENTRIES/2) {
                        node1.children.add(child);
                    } else {
                        node2.children.add(child);
                    }
                }
                
                node1.updateBoundingBox();
                node2.updateBoundingBox();
                
                // Insert the new tile
                RTreeNode bestChild = chooseBestChild(tile.getBoundingBox());
                bestChild.insert(tile);
                updateBoundingBox();
            }
        } else {
            RTreeNode bestChild = chooseBestChild(tile.getBoundingBox());
            bestChild.insert(tile);
            updateBoundingBox();
        }
    }
    public List<Tile> search(BoundingBox searchBox) {
        List<Tile> results = new ArrayList<>();
        
        if (boundingBox == null || !boundingBox.intersects(searchBox)) {
            return results;
        }

        for (RTreeNode child : children) {
            if (child.isLeaf && child.getTile() != null) {
                if (child.getBoundingBox().intersects(searchBox)) {
                    results.add(child.getTile());
                }
            } else {
                results.addAll(child.search(searchBox));
            }
        }
        return results;
    }

    private void split() {
        isLeaf = false;
        List<RTreeNode> newChildren = new ArrayList<>();
        
        // Create two groups based on the furthest pair
        RTreeNode group1 = new RTreeNode();
        RTreeNode group2 = new RTreeNode();
        
        // Find the two nodes that are furthest apart
        double maxDistance = -1;
        RTreeNode seed1 = null, seed2 = null;
        
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
        
        group1.getChildren().add(seed1);
        group2.getChildren().add(seed2);
        
        // Distribute remaining nodes
        for (RTreeNode node : children) {
            if (node != seed1 && node != seed2) {
                double increaseGroup1 = calculateBoundingBoxIncrease(
                    group1.getBoundingBox(), 
                    node.getBoundingBox()
                );
                double increaseGroup2 = calculateBoundingBoxIncrease(
                    group2.getBoundingBox(), 
                    node.getBoundingBox()
                );
                
                if (increaseGroup1 < increaseGroup2) {
                    group1.getChildren().add(node);
                } else {
                    group2.getChildren().add(node);
                }
            }
        }
        
        newChildren.add(group1);
        newChildren.add(group2);
        children = newChildren;
        updateBoundingBox();
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
        
        double minLat = Math.min(current.getSouthWest().getLatitude(), 
                                newBox.getSouthWest().getLatitude());
        double minLon = Math.min(current.getSouthWest().getLongitude(), 
                                newBox.getSouthWest().getLongitude());
        double maxLat = Math.max(current.getNorthEast().getLatitude(), 
                                newBox.getNorthEast().getLatitude());
        double maxLon = Math.max(current.getNorthEast().getLongitude(), 
                                newBox.getNorthEast().getLongitude());
        
        double enlargedArea = Math.abs((maxLat - minLat) * (maxLon - minLon));
        return enlargedArea - currentArea;
    }

    private double calculateArea(BoundingBox box) {
        if (box == null) return 0;
        double latDiff = box.getNorthEast().getLatitude() - box.getSouthWest().getLatitude();
        double lonDiff = box.getNorthEast().getLongitude() - box.getSouthWest().getLongitude();
        return Math.abs(latDiff * lonDiff);
    }

    private void updateBoundingBox() {
        if (children.isEmpty()) return;

        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (RTreeNode child : children) {
            BoundingBox childBox = child.getBoundingBox();
            minLat = Math.min(minLat, childBox.getSouthWest().getLatitude());
            maxLat = Math.max(maxLat, childBox.getNorthEast().getLatitude());
            minLon = Math.min(minLon, childBox.getSouthWest().getLongitude());
            maxLon = Math.max(maxLon, childBox.getNorthEast().getLongitude());
        }

        this.boundingBox = new BoundingBox(
            new Point(minLat, minLon),
            new Point(maxLat, maxLon)
        );
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
}
