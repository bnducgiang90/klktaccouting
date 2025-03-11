package org.klkt.klktaccouting.config;


import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class JsonSchemaValidator {
    private static final Logger log = LoggerFactory.getLogger(JsonSchemaValidator.class);

    private final JsonSchemaFactory schemaFactory;
    private final Map<String, JsonSchema> cache = new ConcurrentHashMap<>();

    public JsonSchemaValidator() {
        // Sử dụng SpecVersion tương ứng với schema của bạn
        schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }

    public void validate(JsonNode data, JsonNode schemaNode) throws JsonSchemaException {
        // Debug logging
        log.info("==== DATA BEING VALIDATED ====");
//        log.info(data.toPrettyString());
//        log.info("==== SCHEMA BEING USED ====");
//        log.info(schemaNode.toPrettyString());

        // Check if the schema is valid
        if (!schemaNode.has("$schema")) {
            log.info("WARNING: Schema doesn't have $schema property");
        }
        if (!schemaNode.has("type")) {
            log.info("WARNING: Schema doesn't have type property");
        }
        try {
            // Ensure we're using the correct schema format
            String schemaKey = schemaNode.toString();
            JsonSchema schema = cache.computeIfAbsent(schemaKey,
                    k -> schemaFactory.getSchema(schemaNode)
            );
//            System.out.println("data:"+ data.toPrettyString());
            Set<ValidationMessage> errors = schema.validate(data);

            if (!errors.isEmpty()) {
                log.info("==== VALIDATION ERRORS ====");
                log.info("ERROR validation: {}", errors);

                String errorMessages = errors.stream()
                        .map(this::formatErrorMessage)
                        .collect(Collectors.joining(", "));
                throw new ValidationException(errorMessages);
            }
        } catch (Exception e) {
            log.info("==== EXCEPTION DURING VALIDATION ====");
//            e.printStackTrace();
            throw e;
        }
    }

    private String formatErrorMessage(ValidationMessage message) {
        return String.format("[%s] %s",
                message.getPath().replace("$.", ""),
                message.getMessage()
        );
    }
}