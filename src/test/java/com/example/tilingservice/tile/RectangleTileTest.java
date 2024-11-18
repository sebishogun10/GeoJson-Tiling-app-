package com.example.tilingservice.tile;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RectangleTileTest {

   private final GeometryFactory geometryFactory = new GeometryFactory();

   @Test
   void testGetCorners() {
       Point sw = new Point(0, 0);
       Point ne = new Point(1, 1);
       BoundingBox box = new BoundingBox(sw, ne);
       RectangleTile tile = new RectangleTile(box);

       List<Point> corners = tile.getCorners();
       
       assertEquals(4, corners.size());
       assertEquals(sw, corners.get(0)); // Southwest
       assertEquals(new Point(1, 0), corners.get(1)); // Northwest
       assertEquals(ne, corners.get(2)); // Northeast
       assertEquals(new Point(0, 1), corners.get(3)); // Southeast
   }

   @Test
   void testGetArea() {
       Point sw = new Point(0, 0);
       Point ne = new Point(2, 3);
       BoundingBox box = new BoundingBox(sw, ne);
       RectangleTile tile = new RectangleTile(box);

       assertEquals(6.0, tile.getArea(), 0.0001);
   }

   @Test
   void testSubdivide() {
       Point sw = new Point(0, 0);
       Point ne = new Point(2, 2);
       BoundingBox box = new BoundingBox(sw, ne);
       RectangleTile tile = new RectangleTile(box);

       List<RectangleTile> subtiles = tile.subdivide();
       
       assertEquals(4, subtiles.size());
       // Check bounds of first subtile (southwest)
       assertEquals(0, subtiles.get(0).getBoundingBox().getSouthWest().getLatitude(), 0.0001);
       assertEquals(0, subtiles.get(0).getBoundingBox().getSouthWest().getLongitude(), 0.0001);
       assertEquals(1, subtiles.get(0).getBoundingBox().getNorthEast().getLatitude(), 0.0001);
       assertEquals(1, subtiles.get(0).getBoundingBox().getNorthEast().getLongitude(), 0.0001);
   }

   @Test
   void testToGeoJson() {
       Point sw = new Point(0, 0);
       Point ne = new Point(1, 1);
       BoundingBox box = new BoundingBox(sw, ne);
       RectangleTile tile = new RectangleTile(box);

       String geoJson = tile.toGeoJson();
       
       assertTrue(geoJson.contains("\"type\":\"Feature\""));
       assertTrue(geoJson.contains("\"type\":\"Polygon\""));
       assertTrue(geoJson.contains("coordinates"));
   }

   @Test
   void testToJtsPolygon() {
       Point sw = new Point(0, 0);
       Point ne = new Point(1, 1);
       BoundingBox box = new BoundingBox(sw, ne);
       RectangleTile tile = new RectangleTile(box);

       Polygon polygon = tile.toJtsPolygon(geometryFactory);
       
       assertNotNull(polygon);
       assertTrue(polygon.isValid());
       assertEquals(5, polygon.getCoordinates().length); // Including closing point
   }
}