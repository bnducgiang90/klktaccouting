package org.klkt.klktaccouting.core.database.impl;

import org.klkt.klktaccouting.core.database.rdbms.DatabaseExecutorFactory;
import org.klkt.klktaccouting.core.database.rdbms.IDatabaseExecutor;
import org.klkt.klktaccouting.service.KLKTCateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.function.Supplier;

@DependsOn("databaseInitializer")
@Configuration
public class DatabaseExecutorImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(KLKTCateService.class);
    @Bean
    public Supplier<IDatabaseExecutor> authDbExecutor() {
        return () -> {
            if (DatabaseExecutorFactory.hasExecutor("authdb")) {
                return DatabaseExecutorFactory.getExecutor("authdb");
            } else {
                // Xử lý trường hợp executor không tồn tại
                throw new RuntimeException("Executor for 'authdb' does not exist");
            }
        };
    }

    @Bean
    public Supplier<IDatabaseExecutor> businessDbExecutor() {
        return () -> {
            if (DatabaseExecutorFactory.hasExecutor("businessdb")) {
                return DatabaseExecutorFactory.getExecutor("businessdb");
            } else {
                // Xử lý trường hợp executor không tồn tại
                throw new RuntimeException("Executor for 'businessdb' does not exist");
            }
        };
    }

}
