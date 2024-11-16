package com.example.tilingservice.service;

import com.example.tilingservice.tile.Tile;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AsyncTileRenderer {
    
    private final ExecutorService executorService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final int CHUNK_SIZE = 1000;
    
    public AsyncTileRenderer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r);
                t.setName("tile-renderer-" + t.getId());
                t.setDaemon(true);
                return t;
            }
        );
    }

    public String renderTilesAsync(List<Tile> tiles) {
        try {
            List<List<Tile>> chunks = chunked(tiles, CHUNK_SIZE);
            List<CompletableFuture<String>> futures = chunks.stream()
                .map(chunk -> CompletableFuture.supplyAsync(
                    () -> renderChunk(chunk),
                    executorService
                ))
                .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );

            return allFutures.thenApply(v -> {
                List<String> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                return combineResults(results);
            }).get(30, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Error rendering tiles: ", e);
            throw new RuntimeException("Failed to render tiles", e);
        }
    }

    public void streamTiles(List<Tile> tiles) {
        List<List<Tile>> chunks = chunked(tiles, CHUNK_SIZE);
        
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < chunks.size(); i++) {
                List<Tile> chunk = chunks.get(i);
                String chunkJson = renderChunk(chunk);
                
                TileChunkMessage message = new TileChunkMessage(
                    i, 
                    chunks.size(), 
                    chunkJson,
                    i == chunks.size() - 1
                );
                
                messagingTemplate.convertAndSend("/topic/tiles/chunk", message);
                
                try {
                    // Small delay to prevent overwhelming the client
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, executorService);
    }

    private String renderChunk(List<Tile> chunk) {
        StringBuilder features = new StringBuilder();
        
        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) features.append(",");
            features.append(chunk.get(i).toGeoJson());
        }
        
        return features.toString();
    }

    private String combineResults(List<String> results) {
        StringBuilder combined = new StringBuilder();
        combined.append("{\"type\":\"FeatureCollection\",\"features\":[");
        
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) combined.append(",");
            combined.append(results.get(i));
        }
        
        combined.append("]}");
        return combined.toString();
    }

    private <T> List<List<T>> chunked(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Data
    @AllArgsConstructor
    private static class TileChunkMessage {
        private int chunkIndex;
        private int totalChunks;
        private String features;
        private boolean isLast;
    }
}