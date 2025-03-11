package org.klkt.klktaccouting.core.database.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.klkt.klktaccouting.core.database.rdbms.AbstractDatabaseExecutor;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MysqlExecutor extends AbstractDatabaseExecutor {
    public MysqlExecutor(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Map<String, Object>> executeProcedure(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = this.prepareProcedureStatement(conn, procedure, params, false, false);
             ResultSet rs = stmt.executeQuery()) {

            return this.mapResultSetToList(rs);
        }
    }

    @Override
    public JsonNode executeProcedureToJson(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = this.prepareProcedureStatement(conn, procedure, params, false, false);
             ResultSet rs = stmt.executeQuery()) {
             if (rs != null) {
                 return this.resultSetToJson(rs);
             }
             return this.objectMapper.createObjectNode();
        }
    }

    @Override
    protected CallableStatement prepareProcedureStatement(Connection conn, String procedure, Map<String, Object> params, boolean hasOutCursor, boolean hasOutputParam) throws SQLException {
        StringBuilder sql = new StringBuilder("{ call ").append(procedure).append("(");
        if (params != null && !params.isEmpty()) {
            sql.append(String.join(",", Collections.nCopies(params.size(), "?")));
        }
        sql.append(") }");

        CallableStatement stmt = conn.prepareCall(sql.toString());
        setParams(stmt, params);
        return stmt;
    }
}
