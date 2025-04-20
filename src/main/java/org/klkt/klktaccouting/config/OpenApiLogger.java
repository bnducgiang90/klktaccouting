package org.klkt.klktaccouting.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OpenApiLogger {
    private static final Logger log = LoggerFactory.getLogger(OpenApiLogger.class);
    private final ApplicationContext applicationContext;

    public OpenApiLogger(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void logApiEndpoints() {

        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Set<String> endpoints = mapping.getHandlerMethods().keySet().stream()
                .map(RequestMappingInfo::toString)
                .collect(Collectors.toSet());
        log.info("Danh sách API Paths: {}", endpoints);
    }
}
