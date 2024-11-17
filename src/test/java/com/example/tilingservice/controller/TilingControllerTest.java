package com.example.tilingservice.controller;

import com.example.tilingservice.service.AsyncTileRenderer;
import com.example.tilingservice.service.TileService;
import com.example.tilingservice.tile.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TilingController.class)
class TilingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TileService tileService;
    
    @MockBean
    private AsyncTileRenderer asyncTileRenderer;

    private String validRequestJson;

    @BeforeEach
    void setUp() {
        validRequestJson = """
            {
                "geoJson": {
                    "type": "Feature",
                    "geometry": {
                        "type": "Polygon",
                        "coordinates": [[[0,0], [0,1], [1,1], [1,0], [0,0]]]
                    }
                },
                "maxTileArea": 1000.0,
                "minTileArea": 10.0,
                "coverageThreshold": 0.10,
                "includeBoundingBox": true
            }
            """;

        when(tileService.generateTiles(
            anyString(),
            anyDouble(),
            anyDouble(),
            anyDouble(),
            anyBoolean()
        )).thenReturn(new ArrayList<Tile>());
        
        when(asyncTileRenderer.renderTilesAsync(anyList()))
            .thenReturn("{}");
    }

    @Test
    @WithMockUser
    void generateTiles_ValidRequest_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/tiles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestJson))
            .andExpect(status().isOk());
    }

    @Test
    void generateTiles_NoAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/tiles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestJson))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void generateTiles_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
                "geoJson": "invalid"
            }
            """;

        mockMvc.perform(post("/api/v1/tiles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}