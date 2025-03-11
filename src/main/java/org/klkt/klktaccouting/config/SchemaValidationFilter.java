package org.klkt.klktaccouting.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaValidationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidationFilter.class);
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
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        String requestBody = new String(wrappedRequest.getCachedBody(), StandardCharsets.UTF_8);
        LOGGER.warn("requestBody: {}", requestBody);

        try {
            if (wrappedRequest.getCachedBody().length > 0) {
                JsonNode requestBodyJson = objectMapper.readTree(requestBody);
                validator.validate(requestBodyJson, schema.schema());
            }

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

    // Custom request wrapper để cache body
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        }

        public byte[] getCachedBody() {
            return this.cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(this.cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
        }

        // Custom ServletInputStream để đọc từ cached body
        private static class CachedBodyServletInputStream extends ServletInputStream {
            private final ByteArrayInputStream byteArrayInputStream;

            public CachedBodyServletInputStream(byte[] cachedBody) {
                this.byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        }
    }
}