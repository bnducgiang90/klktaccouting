package org.klkt.klktaccouting.core.database.impl;

import jakarta.annotation.PostConstruct;
import org.klkt.klktaccouting.core.database.rdbms.DatabaseConfig;
import org.klkt.klktaccouting.core.database.rdbms.DatabaseExecutorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DatabaseInitializer{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final DatabaseConfig databaseConfig;

    public DatabaseInitializer(DatabaseConfig databaseConfig) {
        LOGGER.info("DatabaseInitializer constructor called");
        this.databaseConfig = databaseConfig;
    }

    @PostConstruct
    public void init() {
        try {
            DatabaseExecutorFactory.initializeExecutors(databaseConfig);
            LOGGER.info("✅ Database connections initialized successfully!");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize database connections", e);
            throw e; // Re-throw to make Spring aware of the failure
        }
    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        try {
//            DatabaseExecutorFactory.initializeExecutors(databaseConfig);
//            LOGGER.info("✅ Database connections initialized successfully!");
//        } catch (Exception e) {
//            LOGGER.error("Failed to initialize database connections", e);
//            throw e;
//        }
//    }

}
