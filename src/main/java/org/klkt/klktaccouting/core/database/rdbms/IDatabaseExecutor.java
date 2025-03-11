package org.klkt.klktaccouting.core.database.rdbms;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IDatabaseExecutor {

    List<Map<String, Object>> executeProcedure(String procedure, Map<String, Object> params) throws SQLException;

    void executeProcedureNonQuery(String procedure, Map<String, Object> params) throws SQLException;

    Map<String, Object> executeProcedureWithOutputParams(String procedure, Map<String, Object> params) throws SQLException;

    List<Map<String, Object>> executeQuery(String query, Map<String, Object> params) throws SQLException;

    int executeNonQuery(String query, Map<String, Object> params) throws SQLException;

    JsonNode executeQueryToJson(String sql, Map<String, Object> params) throws SQLException;

    JsonNode executeProcedureToJson(String procedure, Map<String, Object> params) throws SQLException;
}
