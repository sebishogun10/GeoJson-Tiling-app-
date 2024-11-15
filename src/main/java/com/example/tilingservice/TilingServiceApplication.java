package com.example.tilingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Polygon Tiling Service API",
        version = "1.0",
        description = "REST API for generating tile footprints from GeoJSON areas of interest"
    )
)
public class TilingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TilingServiceApplication.class, args);
    }
}
