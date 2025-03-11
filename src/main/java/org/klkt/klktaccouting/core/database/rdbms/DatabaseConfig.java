package org.klkt.klktaccouting.core.database.rdbms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "database")
public class DatabaseConfig {
    private List<DatabaseProperties> connections;

    @Data
    public static class DatabaseProperties {
        private String name;               // Tên database (authdb, businessdb, ...)
        private String type;               // Loại database (mysql, postgres, oracle, ...)
        private String url;                // JDBC URL kết nối
        private String username;           // Tên người dùng
        private String password;           // Mật khẩu
        private boolean initializePool;    // Có khởi tạo pool khi startup hay không

        // Các cấu hình HikariCP pool
        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long idleTimeout = 30000;
        private long maxLifetime = 1800000;
        private long connectionTimeout = 30000;
        private boolean autoCommit = true;
        private String poolName;
        private String driverClassName;
        private long validationTimeout = 5000;
        private long leakDetectionThreshold = 0;
        private boolean readOnly = false;
        private boolean isolationInternalQueries = false;
    }
}
