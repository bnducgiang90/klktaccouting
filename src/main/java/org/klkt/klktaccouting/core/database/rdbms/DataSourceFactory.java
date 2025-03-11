package org.klkt.klktaccouting.core.database.rdbms;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DataSourceFactory {
    public static DataSource createDataSource(DatabaseConfig.DatabaseProperties dbConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbConfig.getUrl());
        hikariConfig.setUsername(dbConfig.getUsername());
        hikariConfig.setPassword(dbConfig.getPassword());

        hikariConfig.setMaximumPoolSize(dbConfig.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(dbConfig.getMinimumIdle());
        hikariConfig.setIdleTimeout(dbConfig.getIdleTimeout());
        hikariConfig.setMaxLifetime(dbConfig.getMaxLifetime());
        hikariConfig.setConnectionTimeout(dbConfig.getConnectionTimeout());

        hikariConfig.setAutoCommit(dbConfig.isAutoCommit());
        hikariConfig.setReadOnly(dbConfig.isReadOnly());
        hikariConfig.setIsolateInternalQueries(dbConfig.isIsolationInternalQueries());

        if (dbConfig.getPoolName() != null) {
            hikariConfig.setPoolName(dbConfig.getPoolName());
        }
        if (dbConfig.getDriverClassName() != null) {
            hikariConfig.setDriverClassName(dbConfig.getDriverClassName());
        }
        if (dbConfig.getLeakDetectionThreshold() > 0) {
            hikariConfig.setLeakDetectionThreshold(dbConfig.getLeakDetectionThreshold());
        }

        return new HikariDataSource(hikariConfig);
    }
}
