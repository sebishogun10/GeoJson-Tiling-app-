package com.example.tilingservice.rtree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;

@Component
public class RTreeSerializer {
    private static final String DATA_FILE = "data/rtree-data.json";
    private final ObjectMapper objectMapper;

    public RTreeSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    public void serialize(RTree rtree) throws IOException {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writeValue(file, rtree);
    }

    public RTree deserialize() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new RTree();
        }
        return objectMapper.readValue(file, RTree.class);
    }
}
