package com.example.tilingservice.controller;

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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TilingController.class)
class TilingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TileService tileService;

    private String validGeoJson;

    @BeforeEach
    void setUp() {
        validGeoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[0,0], [0,1], [1,1], [1,0], [0,0]]]
                }
            }
            """;
        
        when(tileService.generateTiles(anyString())).thenReturn(new ArrayList<Tile>());
        when(tileService.generateGeoJson(new ArrayList<>())).thenReturn("{}");
    }

    @Test
    @WithMockUser
    void generateTiles_ValidGeoJson_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/tiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validGeoJson))
                .andExpect(status().isOk());
    }

    @Test
    void generateTiles_NoAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/tiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validGeoJson))
                .andExpect(status().isUnauthorized());
    }
}
