package com.example.tilingservice.tile;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class RectangleTile implements Tile {
    private BoundingBox boundingBox;

    public RectangleTile(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public List<Point> getCorners() {
        List<Point> corners = new ArrayList<>();
        Point sw = boundingBox.getSouthWest();
        Point ne = boundingBox.getNorthEast();
        
        corners.add(sw); // Southwest
        corners.add(new Point(ne.getLatitude(), sw.getLongitude())); // Northwest
        corners.add(ne); // Northeast
        corners.add(new Point(sw.getLatitude(), ne.getLongitude())); // Southeast
        
        return corners;
    }

    @Override
    public double getArea() {
        double latDiff = boundingBox.getNorthEast().getLatitude() - 
                        boundingBox.getSouthWest().getLatitude();
        double lonDiff = boundingBox.getNorthEast().getLongitude() - 
                        boundingBox.getSouthWest().getLongitude();
        return Math.abs(latDiff * lonDiff);
    }

    @Override
    public List<RectangleTile> subdivide() {
        List<RectangleTile> subtiles = new ArrayList<>();
        Point sw = boundingBox.getSouthWest();
        Point ne = boundingBox.getNorthEast();
        
        double midLat = (sw.getLatitude() + ne.getLatitude()) / 2;
        double midLon = (sw.getLongitude() + ne.getLongitude()) / 2;

        // Southwest tile
        subtiles.add(new RectangleTile(new BoundingBox(
            new Point(sw.getLatitude(), sw.getLongitude()),
            new Point(midLat, midLon)
        )));

        // Northwest tile
        subtiles.add(new RectangleTile(new BoundingBox(
            new Point(midLat, sw.getLongitude()),
            new Point(ne.getLatitude(), midLon)
        )));

        // Northeast tile
        subtiles.add(new RectangleTile(new BoundingBox(
            new Point(midLat, midLon),
            new Point(ne.getLatitude(), ne.getLongitude())
        )));

        // Southeast tile
        subtiles.add(new RectangleTile(new BoundingBox(
            new Point(sw.getLatitude(), midLon),
            new Point(midLat, ne.getLongitude())
        )));

        return subtiles;
    }

    @Override
    public String toGeoJson() {
        List<Point> corners = getCorners();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[");
        
        for (int i = 0; i < corners.size(); i++) {
            Point p = corners.get(i);
            sb.append("[").append(p.getLongitude()).append(",").append(p.getLatitude()).append("]");
            if (i < corners.size() - 1) {
                sb.append(",");
            }
        }
        // Close the polygon by repeating the first point
        sb.append(",[").append(corners.get(0).getLongitude())
          .append(",").append(corners.get(0).getLatitude()).append("]");
        
        sb.append("]]},\"properties\":{}}");
        return sb.toString();
    }
}
