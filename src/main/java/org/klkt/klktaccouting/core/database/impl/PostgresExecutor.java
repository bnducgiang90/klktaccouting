package org.klkt.klktaccouting.core.database.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.klkt.klktaccouting.core.database.rdbms.AbstractDatabaseExecutor;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class PostgresExecutor extends AbstractDatabaseExecutor {

    public PostgresExecutor(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Map<String, Object>> executeProcedure(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = this.prepareProcedureStatement(conn, procedure, params, true, false)) {
            stmt.execute();
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                return this.mapResultSetToList(rs);
            }
        }
    }


    @Override
    public JsonNode executeProcedureToJson(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = this.prepareProcedureStatement(conn, procedure, params, true, false)) {
            boolean hasData = stmt.execute();
            if (hasData) {
                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    return this.resultSetToJson(rs);
                }
            }
            return this.objectMapper.createObjectNode();
        }
    }

    @Override
    protected CallableStatement prepareProcedureStatement(Connection conn, String procedure, Map<String, Object> params, boolean hasOutCursor, boolean hasOutputParam) throws SQLException {
        String sql = "{ call " + procedure + "(?) }";
        CallableStatement stmt = conn.prepareCall(sql);
        if (hasOutCursor) stmt.registerOutParameter(1, Types.REF_CURSOR);
        setParams(stmt, params);
        return stmt;
    }

}
