package com.example.tilingservice.geojson;

import com.example.tilingservice.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class GeoJsonParser implements Parser {
    private final ObjectMapper objectMapper;

    public GeoJsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Shape parse(String geoJson) {
        try {
            JsonNode root = objectMapper.readTree(geoJson);
            
            // Handle Feature type
            if (root.has("type") && "Feature".equals(root.get("type").asText())) {
                root = root.get("geometry");
            }
            
            String type = root.get("type").asText();

            switch (type) {
                case "Polygon":
                    return parsePolygon(root.get("coordinates"));
                case "MultiPolygon":
                    return parseMultiPolygon(root.get("coordinates"));
                default:
                    throw new IllegalArgumentException("Unsupported geometry type: " + type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid GeoJSON format: " + e.getMessage(), e);
        }
    }

    private PolygonShape parsePolygon(JsonNode coordinates) {
        List<List<Point>> rings = parseCoordinateRings(coordinates);
        return new PolygonShape(rings.get(0), rings.subList(1, rings.size()));
    }

    private MultiPolygonShape parseMultiPolygon(JsonNode coordinates) {
        List<PolygonShape> polygons = new ArrayList<>();
        for (JsonNode polygonCoords : coordinates) {
            List<List<Point>> rings = parseCoordinateRings(polygonCoords);
            polygons.add(new PolygonShape(rings.get(0), rings.subList(1, rings.size())));
        }
        return new MultiPolygonShape(polygons);
    }

    private List<List<Point>> parseCoordinateRings(JsonNode rings) {
        List<List<Point>> result = new ArrayList<>();
        for (JsonNode ring : rings) {
            List<Point> points = new ArrayList<>();
            for (JsonNode coord : ring) {
                points.add(new Point(
                    coord.get(1).asDouble(), // latitude
                    coord.get(0).asDouble()  // longitude
                ));
            }
            result.add(points);
        }
        return result;
    }
}
