package com.example.tilingservice.utils;

import com.example.tilingservice.model.BoundingBox;
import com.example.tilingservice.model.Point;
import java.util.List;

public class GeometryUtils {
    public static boolean pointInPolygon(Point point, List<Point> polygon) {
        boolean inside = false;
        int i, j;
        for (i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            Point pi = polygon.get(i);
            Point pj = polygon.get(j);
            
            if (((pi.getLatitude() > point.getLatitude()) != (pj.getLatitude() > point.getLatitude())) &&
                (point.getLongitude() < (pj.getLongitude() - pi.getLongitude()) * 
                (point.getLatitude() - pi.getLatitude()) / (pj.getLatitude() - pi.getLatitude()) + 
                pi.getLongitude())) {
                inside = !inside;
            }
        }
        return inside;
    }

    public static double calculatePolygonArea(List<Point> polygon) {
        double area = 0;
        int j;
        for (int i = 0; i < polygon.size(); i++) {
            j = (i + 1) % polygon.size();
            area += polygon.get(i).getLongitude() * polygon.get(j).getLatitude();
            area -= polygon.get(j).getLongitude() * polygon.get(i).getLatitude();
        }
        area = Math.abs(area) / 2.0;
        return area;
    }

    public static double calculateDistance(Point p1, Point p2) {
        double R = 6371e3; // Earth's radius in meters
        double lat1 = Math.toRadians(p1.getLatitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double deltaLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double deltaLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                  Math.cos(lat1) * Math.cos(lat2) *
                  Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

    public static double calculateAreaInMeters(BoundingBox box) {
        Point sw = box.getSouthWest();
        Point ne = box.getNorthEast();
   
   // Haversine formula components
        double dLat = Math.toRadians(ne.getLatitude() - sw.getLatitude());
        double dLon = Math.toRadians(ne.getLongitude() - sw.getLongitude());
        double lat1 = Math.toRadians(sw.getLatitude());
        double lat2 = Math.toRadians(ne.getLatitude());
   
   // Earth's radius in meters
        double R = 6371e3;
   
   // Calculate width and height in meters
        double width = R * Math.cos(lat1) * dLon;
        double height = R * dLat;
   
        return Math.abs(width * height);
}


}
