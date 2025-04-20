package org.klkt.klktaccouting.config.schemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ApiSchemaLoader {
    private final List<ApiSchema> schemas;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiSchemaLoader(ObjectMapper objectMapper) throws IOException {
        Resource resource = new ClassPathResource("api-schemas.json");
        schemas = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {}
        );
    }

    public Optional<ApiSchema> findSchema(HttpServletRequest request) {
        String path = request.getRequestURI();
        System.out.println("findSchema: " + path);
        String method = request.getMethod().toUpperCase();

        return schemas.stream()
                .filter(schema ->
                        pathMatcher.match(schema.apiRouter(), path) &&
                                schema.method().name().equals(method)
                )
                .findFirst();
    }
}