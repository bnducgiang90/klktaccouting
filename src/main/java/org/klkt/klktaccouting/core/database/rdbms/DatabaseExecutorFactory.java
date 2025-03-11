package org.klkt.klktaccouting.core.database.rdbms;

import org.klkt.klktaccouting.core.database.impl.MysqlExecutor;
import org.klkt.klktaccouting.core.database.impl.OracleExecutor;
import org.klkt.klktaccouting.core.database.impl.PostgresExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DatabaseExecutorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseExecutorFactory.class);
    private static final Map<String, IDatabaseExecutor> executorMap = new ConcurrentHashMap<>();

    public static void initializeExecutors(DatabaseConfig databaseConfig) {
        LOGGER.info("Initializing database executors configuration");

        for (DatabaseConfig.DatabaseProperties dbConfig : databaseConfig.getConnections()) {
            if (dbConfig.isInitializePool()) {
                DataSource dataSource = DataSourceFactory.createDataSource(dbConfig);
                DatabaseType dbType = detectDatabaseType(dbConfig.getUrl()); // Tự động nhận diện loại DB
//                executorMap.put(dbConfig.getName(), createExecutor(DatabaseType.fromString(dbConfig.getType()), dataSource));
                IDatabaseExecutor executor = createExecutor(dbType, dataSource);
                executorMap.put(dbConfig.getName(), executor);
                LOGGER.info("Executor initialized pool for database: {}", dbConfig.getName());
            } else {
                LOGGER.info("Skipped initialized pool for database: {}", dbConfig.getName());
            }
        }
    }

    public static IDatabaseExecutor getExecutor(String dbName) {
        IDatabaseExecutor executor = executorMap.get(dbName);
        if (executor == null) {
            throw new IllegalArgumentException("No executor found for database: " + dbName);
        }
        return executor;
    }

    private static IDatabaseExecutor createExecutor(DatabaseType dbType, DataSource dataSource) {
        switch (dbType) {
            case POSTGRES:
                return new PostgresExecutor(dataSource);
            case ORACLE:
                return new OracleExecutor(dataSource);
            case MYSQL:
                return new MysqlExecutor(dataSource);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    public static boolean hasExecutor(String dbName) {
        return executorMap.containsKey(dbName);
    }

    private static DatabaseType detectDatabaseType(String url) {
        if (url.contains(":postgresql:")) return DatabaseType.POSTGRES;
        if (url.contains(":mysql:")) return DatabaseType.MYSQL;
        if (url.contains(":oracle:")) return DatabaseType.ORACLE;
        throw new IllegalArgumentException("Unknown database type in URL: " + url);
    }

}
