package com.example.tilingservice.utils;

import com.example.tilingservice.model.Point;
import java.util.List;

public class ValidationUtils {
    public static boolean isValidPolygon(List<Point> polygon) {
        if (polygon == null || polygon.size() < 3) {
            return false;
        }
 
        // Check if the polygon is closed
        Point first = polygon.get(0);
        Point last = polygon.get(polygon.size() - 1);
 
        return isPointsEqual(first, last) && !hasSelfIntersections(polygon);
    }
 
    private static boolean isPointsEqual(Point p1, Point p2) {
        double epsilon = 1e-10;
        return Math.abs(p1.getLatitude() - p2.getLatitude()) < epsilon &&
               Math.abs(p1.getLongitude() - p2.getLongitude()) < epsilon;
    }
 
    private static boolean hasSelfIntersections(List<Point> polygon) {
        int n = polygon.size();
        for (int i = 0; i < n-1; i++) {
            for (int j = i+2; j < n-1; j++) {
                if (i == 0 && j == n-2) continue; // Skip first/last edge comparison
                
                Point p1 = polygon.get(i);
                Point p2 = polygon.get(i+1);
                Point p3 = polygon.get(j);
                Point p4 = polygon.get(j+1);
                
                if (segmentsIntersect(p1, p2, p3, p4)) {
                    return true;
                }
            }
        }
        return false;
    }
 
    private static boolean segmentsIntersect(Point p1, Point p2, Point p3, Point p4) {
        double epsilon = 1e-10;
 
        // Calculate orientations
        double o1 = orientation(p1, p2, p3);
        double o2 = orientation(p1, p2, p4);
        double o3 = orientation(p3, p4, p1);
        double o4 = orientation(p3, p4, p2);
 
        // General case
        if (o1 * o2 < 0 && o3 * o4 < 0) return true;
 
        // Special cases for collinear points
        if (Math.abs(o1) < epsilon && onSegment(p1, p2, p3)) return true;
        if (Math.abs(o2) < epsilon && onSegment(p1, p2, p4)) return true;
        if (Math.abs(o3) < epsilon && onSegment(p3, p4, p1)) return true;
        if (Math.abs(o4) < epsilon && onSegment(p3, p4, p2)) return true;
 
        return false;
    }
 
    private static double orientation(Point p, Point q, Point r) {
        return (q.getLatitude() - p.getLatitude()) * (r.getLongitude() - q.getLongitude()) -
               (q.getLongitude() - p.getLongitude()) * (r.getLatitude() - q.getLatitude());
    }
 
    private static boolean onSegment(Point p, Point q, Point r) {
        return r.getLongitude() <= Math.max(p.getLongitude(), q.getLongitude()) &&
               r.getLongitude() >= Math.min(p.getLongitude(), q.getLongitude()) &&
               r.getLatitude() <= Math.max(p.getLatitude(), q.getLatitude()) &&
               r.getLatitude() >= Math.min(p.getLatitude(), q.getLatitude());
    }
 }