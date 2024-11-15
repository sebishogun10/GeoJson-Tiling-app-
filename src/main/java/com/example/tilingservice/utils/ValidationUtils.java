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
        if (first.getLatitude() != last.getLatitude() || 
            first.getLongitude() != last.getLongitude()) {
            return false;
        }

        // Check for self-intersections
        return !hasSelfIntersections(polygon);
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

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        return d1 == 0 && onSegment(p3, p4, p1) ||
               d2 == 0 && onSegment(p3, p4, p2) ||
               d3 == 0 && onSegment(p1, p2, p3) ||
               d4 == 0 && onSegment(p1, p2, p4);
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
