package org.klkt.klktaccouting.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
//import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Configuration
public class OpenApiConfig {

    private final ObjectMapper objectMapper;

    public OpenApiConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            List<ApiSchema> apiSchemas = null;
            try {
                apiSchemas = loadApiSchemas();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            apiSchemas.forEach(api -> addCustomPath(openApi, api));

            // ✅ Dùng đúng SecurityScheme từ models
            // ✅ Tạo SecurityScheme JWT đúng cách
            io.swagger.v3.oas.models.security.SecurityScheme bearerAuthScheme = new io.swagger.v3.oas.models.security.SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(SecurityScheme.In.HEADER)
                    .name("Authorization");

            openApi.components(
                    new Components().addSecuritySchemes("bearerAuth", bearerAuthScheme)
            ).addSecurityItem(
                    new SecurityRequirement().addList("bearerAuth")
            );

        };
    }

    private List<ApiSchema> loadApiSchemas() throws IOException {
        Resource resource = new ClassPathResource("api-schemas.json");
        return objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {}
        );
    }

    private void addCustomPath(OpenAPI openApi, ApiSchema api) {
        Operation operation = new Operation();

        // Request Body
        if (api.schema() != null) {
            RequestBody requestBody = createRequestBody(api.schema(), api.example());
            operation.setRequestBody(requestBody);
        }

        // Responses
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", createResponse(api.output200()));
        responses.addApiResponse("default", createResponse(api.outputNot200()));
        operation.setResponses(responses);

        // Path Parameters
        extractPathParams(api.apiRouter()).forEach(operation::addParametersItem);

        // Add to PathItem
        PathItem pathItem = new PathItem();
        switch (api.method()) {
            case PUT -> pathItem.put(operation);
            case POST -> pathItem.post(operation);
            case GET -> pathItem.get(operation);
            case DELETE -> pathItem.delete(operation);
            case PATCH -> pathItem.patch(operation);
        }

        openApi.getPaths().addPathItem(api.apiRouter(), pathItem);
    }

    private RequestBody createRequestBody(JsonNode schemaNode, Map<String, Object> example) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(convertJsonSchemaToOpenApiSchema(schemaNode));
        mediaType.setExample(example);
        content.addMediaType("application/json", mediaType);
        return new RequestBody().content(content);
    }

    private Schema<?> convertJsonSchemaToOpenApiSchema(JsonNode jsonSchema) {
        Schema<?> schema = new Schema<>();
        schema.setType(jsonSchema.get("type").asText());

        // Handle properties
        JsonNode properties = jsonSchema.get("properties");
        if (properties != null) {
            Map<String, Schema> props = new HashMap<>();
            properties.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldSchema = entry.getValue();
                props.put(fieldName, convertJsonFieldToSchema(fieldSchema));
            });
            schema.setProperties(props);
        }

        // Handle required fields
        JsonNode required = jsonSchema.get("required");
        if (required != null && required.isArray()) {
            List<String> requiredFields = new ArrayList<>();
            required.forEach(node -> requiredFields.add(node.asText()));
            schema.setRequired(requiredFields);
        }

        return schema;
    }

    private Schema<?> convertJsonFieldToSchema(JsonNode fieldSchema) {
        String type = fieldSchema.get("type").asText();
        Schema<?> schema = switch (type) {
            case "string" -> {
                StringSchema stringSchema = new StringSchema();
                if (fieldSchema.has("format"))
                    stringSchema.setFormat(fieldSchema.get("format").asText());
                if (fieldSchema.has("pattern"))
                    stringSchema.setPattern(fieldSchema.get("pattern").asText());
                yield stringSchema;
            }
            case "number" -> {
                NumberSchema numberSchema = new NumberSchema();
                if (fieldSchema.has("minimum"))
                    numberSchema.setMinimum(new BigDecimal(fieldSchema.get("minimum").asDouble()));
                if (fieldSchema.has("maximum"))
                    numberSchema.setMaximum(new BigDecimal(fieldSchema.get("maximum").asDouble()));
                yield numberSchema;
            }
            case "integer" -> {
                IntegerSchema integerSchema = new IntegerSchema();
                if (fieldSchema.has("minimum"))
                    integerSchema.setMinimum(new BigDecimal(fieldSchema.get("minimum").asInt()));
                if (fieldSchema.has("maximum"))
                    integerSchema.setMaximum(new BigDecimal(fieldSchema.get("maximum").asInt()));
                yield integerSchema;
            }
            case "array" -> {
                ArraySchema arraySchema = new ArraySchema();
                if (fieldSchema.has("items"))
                    arraySchema.setItems(convertJsonFieldToSchema(fieldSchema.get("items")));
                yield arraySchema;
            }
            case "object" -> {
                ObjectSchema objectSchema = new ObjectSchema();
                if (fieldSchema.has("properties")) {
                    Map<String, Schema> props = new HashMap<>();
                    fieldSchema.get("properties").fields().forEachRemaining(entry -> {
                        props.put(entry.getKey(), convertJsonFieldToSchema(entry.getValue()));
                    });
                    objectSchema.setProperties(props);
                }
                yield objectSchema;
            }
            default -> new Schema<>().type(type);
        };
        return schema;
    }

    private ApiResponse createResponse(Map<String, Object> outputExample) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setExample(outputExample);
        content.addMediaType("application/json", mediaType);
        return new ApiResponse().content(content);
    }

    private List<Parameter> extractPathParams(String apiRouter) {
        return Arrays.stream(apiRouter.split("/"))
                .filter(part -> part.startsWith("{") && part.endsWith("}"))
                .map(part -> new Parameter()
                        .name(part.substring(1, part.length() - 1))
                        .in(ParameterIn.PATH.toString())
                        .required(true)
                        .schema(new StringSchema())
                )
                .collect(Collectors.toList());
    }
}