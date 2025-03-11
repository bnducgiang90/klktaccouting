package org.klkt.klktaccouting.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaValidationFilter extends OncePerRequestFilter {
    private final ApiSchemaLoader schemaLoader;
    private final JsonSchemaValidator validator;
    private final ObjectMapper objectMapper;

    public SchemaValidationFilter(ApiSchemaLoader schemaLoader,
                                  JsonSchemaValidator validator,
                                  ObjectMapper objectMapper) {
        this.schemaLoader = schemaLoader;
        this.validator = validator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Optional<ApiSchema> schemaOpt = schemaLoader.findSchema(request);
        if (schemaOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        ApiSchema schema = schemaOpt.get();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        // Read the body content from the input stream, not from getContentAsByteArray()
        String requestBody = StreamUtils.copyToString(wrappedRequest.getInputStream(), StandardCharsets.UTF_8);

        try {
            // Only proceed with validation if there's a body to validate
            if (!requestBody.isEmpty()) {
                JsonNode requestBodyJson = objectMapper.readTree(requestBody);
                validator.validate(requestBodyJson, schema.schema());
            }

            // Important: Use the wrapped request to preserve the body for downstream filters/controllers
            filterChain.doFilter(wrappedRequest, response);
        } catch (JsonProcessingException e) {
            sendError(response, "Invalid JSON format: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ValidationException e) {
            sendError(response, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void sendError(HttpServletResponse response, String message, HttpStatus status)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(Map.of(
                        "error", message,
                        "status", status.value()
                ))
        );
    }
}