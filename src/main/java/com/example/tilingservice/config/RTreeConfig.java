package com.example.tilingservice.config;

import com.example.tilingservice.rtree.RTree;
import com.example.tilingservice.tile.RectangleTile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RTreeConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(RectangleTile.class);
        return mapper;
    }
    
    @Bean
    public RTree rTree() {
        return new RTree();
    }
}
