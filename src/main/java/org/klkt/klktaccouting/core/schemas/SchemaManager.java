package org.klkt.klktaccouting.core.schemas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.klkt.klktaccouting.service.KLKTCateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class SchemaManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaManager.class);
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private JsonNode schemasArray;
    private Map<String, JsonNode> schemaMap = new HashMap<>();

    @Autowired
    public SchemaManager(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            // Load JSON schema từ classpath
            Resource resource = resourceLoader.getResource("classpath:/api-schemas.json");
            InputStream inputStream = resource.getInputStream();

            // Đọc và parse JSON
            schemasArray = objectMapper.readTree(inputStream);

            // Tạo map để dễ truy xuất theo path
            if (schemasArray.isArray()) {
                for (JsonNode apiDef : schemasArray) {
                    if (apiDef.has("api_router")) {
                        String path = apiDef.get("api_router").asText();
                        schemaMap.put(path, apiDef);
                    }
                }
            }
        } catch (IOException e) {
            // Log error, schema loading failed
            LOGGER.error("Error loading schema from classpath: {}", e.getMessage());
        }
    }

    /**
     * Trả về mảng JSON chứa toàn bộ các định nghĩa API
     */
    public JsonNode getSchemasAsJsonNode() {
        return schemasArray;
    }

    /**
     * Trả về map các schema theo path
     */
    public Map<String, JsonNode> getSchemas() {
        return schemaMap;
    }

    /**
     * Lấy schema cho một API path cụ thể
     */
    public JsonNode getSchema(String path) {
        return schemaMap.get(path);
    }
}
