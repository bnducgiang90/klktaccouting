package org.klkt.klktaccouting.core.database.rdbms;

public enum DatabaseType {
    POSTGRES, ORACLE, MYSQL;

    public static DatabaseType fromString(String dbType) {
        try {
            return DatabaseType.valueOf(dbType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}