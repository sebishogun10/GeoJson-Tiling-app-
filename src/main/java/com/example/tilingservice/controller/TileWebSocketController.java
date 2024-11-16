package com.example.tilingservice.controller;

import com.example.tilingservice.service.AsyncTileRenderer;
import com.example.tilingservice.service.TileService;
import com.example.tilingservice.tile.Tile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TileWebSocketController {

    private final TileService tileService;
    private final AsyncTileRenderer tileRenderer;

    @MessageMapping("/tiles")
    @SendTo("/topic/tiles")
    public void streamTiles(String geoJson) {
        List<Tile> tiles = tileService.generateTiles(geoJson);
        tileRenderer.streamTiles(tiles);
    }
}