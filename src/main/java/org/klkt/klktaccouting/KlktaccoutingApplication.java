package org.klkt.klktaccouting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@EnableConfigurationProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class KlktaccoutingApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(KlktaccoutingApplication.class);

    public static void main(String[] args) {
//		SpringApplication.run(KlktaccoutingApplication.class, args);

        ConfigurableApplicationContext context = SpringApplication.run(KlktaccoutingApplication.class, args);
        LOGGER.info("��� KLKTaccouting Application started");


//
//        // Khởi tạo Database Executors sau khi Spring context đã được tạo
//        try {
//            LOGGER.info("Manually initializing database executors");
//            DatabaseConfig databaseConfig = context.getBean(DatabaseConfig.class);
//            if (databaseConfig == null) {
//                LOGGER.error("DatabaseConfig bean is null");
//            } else if (databaseConfig.getConnections() == null) {
//                LOGGER.error("DatabaseConfig connections is null");
//            } else {
//                LOGGER.info("Found {} database connections in config", databaseConfig.getConnections().size());
//                DatabaseExecutorFactory.initializeExecutors(databaseConfig);
//                LOGGER.info("✅ Database executors initialized manually from main method");
//            }
//        } catch (Exception e) {
//            LOGGER.error("Error initializing database executors: {}", e.getMessage(), e);
//        }
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/webjars/**")
                        .addResourceLocations("classpath:/META-INF/resources/webjars/");
            }
        };
    }
}
