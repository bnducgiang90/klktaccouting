package org.klkt.klktaccouting.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public record ApiSchema(
        @JsonProperty("api_router") String apiRouter,
        @JsonProperty("method") Method method,
        @JsonProperty("schema") JsonNode schema,
        @JsonProperty("example") Map<String, Object> example,
        @JsonProperty("output_200") Map<String, Object> output200,
        @JsonProperty("output_not_200") Map<String, Object> outputNot200
) {
    public enum Method {
        GET, POST, PUT, DELETE, PATCH
    }
}

//public record ApiSchema(
//        String api_router,
//        Method method,
//        JsonNode schema, // Sử dụng JsonNode thay vì Schema custom
//        Map<String, Object> example,
//        Map<String, Object> output_200,
//        Map<String, Object> output_not_200
//) {
//    public enum Method {
//        GET, POST, PUT, DELETE, PATCH
//    }
//}