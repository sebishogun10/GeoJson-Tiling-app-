package com.example.tilingservice.config;

import com.example.tilingservice.tile.RectangleTile;
import com.example.tilingservice.tile.Tile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(new NamedType(RectangleTile.class, "rectangle"));
        return mapper;
    }
}
