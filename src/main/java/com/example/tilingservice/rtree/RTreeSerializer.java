package com.example.tilingservice.rtree;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;

@Component
public class RTreeSerializer {
    private static final String DATA_FILE = "data/rtree-data.json";
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(RTreeSerializer.class);

    public RTreeSerializer() {
        this.objectMapper = new ObjectMapper();
        // Configure ObjectMapper to handle RTree serialization
        this.objectMapper.enableDefaultTyping(); // for handling polymorphic types
    }

    public void serialize(RTree rtree) throws IOException {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();
        try {
            objectMapper.writeValue(file, rtree);
            logger.info("RTree serialized to {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to serialize RTree: {}", e.getMessage());
            throw e;
        }
    }

    public RTree deserialize() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            logger.info("No existing RTree data found at {}", file.getAbsolutePath());
            return new RTree();
        }
        try {
            return objectMapper.readValue(file, RTree.class);
        } catch (IOException e) {
            logger.error("Failed to deserialize RTree: {}", e.getMessage());
            throw e;
        }
    }
}
