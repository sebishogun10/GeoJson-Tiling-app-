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
        
        if (!isPointsEqual(first, last)) {
            return false;
        }

        return !hasSelfIntersections(polygon);
    }

    private static boolean isPointsEqual(Point p1, Point p2) {
        double epsilon = 1e-10;
        return Math.abs(p1.getLatitude() - p2.getLatitude()) < epsilon && 
               Math.abs(p1.getLongitude() - p2.getLongitude()) < epsilon;
    }

    private static boolean hasSelfIntersections(List<Point> polygon) {
        for (int i = 0; i < polygon.size() - 1; i++) {
            for (int j = i + 2; j < polygon.size() - 1; j++) {
                if (segmentsIntersect(
                        polygon.get(i), polygon.get(i + 1),
                        polygon.get(j), polygon.get(j + 1))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean segmentsIntersect(Point p1, Point p2, Point p3, Point p4) {
        double d1 = direction(p3, p4, p1);
        double d2 = direction(p3, p4, p2);
        double d3 = direction(p1, p2, p3);
        double d4 = direction(p1, p2, p4);

        double epsilon = 1e-10;

        if (Math.abs(d1) < epsilon) return onSegment(p3, p4, p1);
        if (Math.abs(d2) < epsilon) return onSegment(p3, p4, p2);
        if (Math.abs(d3) < epsilon) return onSegment(p1, p2, p3);
        if (Math.abs(d4) < epsilon) return onSegment(p1, p2, p4);

        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
               ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0));
    }

    private static double direction(Point pi, Point pj, Point pk) {
        return (pk.getLongitude() - pi.getLongitude()) * (pj.getLatitude() - pi.getLatitude()) -
               (pj.getLongitude() - pi.getLongitude()) * (pk.getLatitude() - pi.getLatitude());
    }

    private static boolean onSegment(Point pi, Point pj, Point pk) {
        return Math.min(pi.getLongitude(), pj.getLongitude()) <= pk.getLongitude() &&
               pk.getLongitude() <= Math.max(pi.getLongitude(), pj.getLongitude()) &&
               Math.min(pi.getLatitude(), pj.getLatitude()) <= pk.getLatitude() &&
               pk.getLatitude() <= Math.max(pi.getLatitude(), pj.getLatitude());
    }
}
