package com.example.tilingservice.geojson;

import com.example.tilingservice.model.Shape;

public interface Parser {
    Shape parse(String geoJson);
}
