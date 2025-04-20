package org.klkt.klktaccouting.core.database.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.klkt.klktaccouting.core.database.rdbms.AbstractDatabaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class PostgresExecutor extends AbstractDatabaseExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresExecutor.class);

    public PostgresExecutor(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Map<String, Object>> executeProcedure(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection()) {
            // Tắt auto-commit để giữ cursor mở
            conn.setAutoCommit(false);
            try (
                    CallableStatement stmt = this.prepareProcedureStatement(conn, procedure, params, true, false)
            ) {
                LOGGER.info("executeProcedure: {}", stmt);
                stmt.execute();
                try (ResultSet rs = (ResultSet) stmt.getObject(params.size() + 1)) {
                    List<Map<String, Object>> result = mapResultSetToList(rs);

                    // Commit transaction để đóng cursor
                    conn.commit();
                    return result;
                }
            }

        } catch (Exception e) {
            throw new SQLException("Error converting params to JSON", e);

        }
    }


    @Override
    public JsonNode executeProcedureToJson(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = this.prepareProcedureStatement(conn, procedure, params, true, false)) {
            LOGGER.info("executeProcedureToJson: {}", stmt);
            boolean hasData = stmt.execute();
            if (hasData) {
                try (ResultSet rs = (ResultSet) stmt.getObject(params.size() + 1)) {
                    return this.resultSetToJson(rs);
                }
            }
            return this.objectMapper.createObjectNode();
        }
    }

    @Override
    protected CallableStatement prepareProcedureStatement(Connection conn, String procedure, Map<String, Object> params, boolean hasOutCursor, boolean hasOutputParam) throws SQLException {
        String sql;
        int paramCount = params != null ? params.size() : 0;

        // Nếu có out cursor, thì thêm 1 chỗ cho nó ở đầu
        if (hasOutCursor) {
            sql = "CALL " + procedure + "(" + String.join(",", Collections.nCopies(paramCount + 1, "?")) + ")";
        } else {
            sql = "CALL " + procedure + "(" + String.join(",", Collections.nCopies(paramCount, "?")) + ")";
        }
        LOGGER.info("PostgresExecutor - Using procedure: {}", procedure);
        LOGGER.info("Generated SQL: {}", sql);
        CallableStatement stmt = conn.prepareCall(sql);
        this.setParams(stmt, params);
        if (hasOutCursor) stmt.registerOutParameter(params.size() + 1, Types.REF_CURSOR);
        return stmt;
    }

}
