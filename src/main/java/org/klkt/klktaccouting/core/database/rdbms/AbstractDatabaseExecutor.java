package org.klkt.klktaccouting.core.database.rdbms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractDatabaseExecutor implements IDatabaseExecutor {
    protected final DataSource dataSource;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseExecutor.class);
    private static final Map<String, List<Map<String, Object>>> queryCache = new ConcurrentHashMap<>();


    public AbstractDatabaseExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void executeProcedureNonQuery(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = this.prepareProcedureStatement(conn, procedure, params, false, false)) {
            LOGGER.info("executeProcedureNonQuery: {}", stmt);
            stmt.executeUpdate();
        }
    }

    @Override
    // Execute INSERT, UPDATE, DELETE queries
    public int executeNonQuery(String query, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            LOGGER.info("executeNonQuery stmt: {}", stmt);
            setParams(stmt, params);
            return stmt.executeUpdate();
        }
    }


    @Override
    public List<Map<String, Object>> executeQuery(String query, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            LOGGER.info("executeQuery stmt: {}", stmt);
            setParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetToList(rs);
            }
        }
    }

    @Override
    public Map<String, Object> executeProcedureWithOutputParams(String procedure, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = prepareProcedureStatement(conn, procedure, params, false, true)) {
            LOGGER.info("executeProcedureWithOutputParams stmt: {}", stmt);
            stmt.execute();
            Map<String, Object> result = new HashMap<>();
            String  out_data = stmt.getString(params.size() + 1);
            LOGGER.warn("out_data: {}", out_data);
            try {
                JsonNode jsonValue = objectMapper.readTree(out_data);
                LOGGER.warn("out_data jsonValue: {}", jsonValue);
                result.put("p_output", jsonValue);
            } catch (JsonProcessingException e) {
                LOGGER.error("Lỗi parse JSON cho cột {}", out_data, e);
                result.put("p_output", out_data); // Giữ nguyên giá trị chuỗi nếu không thể parse
            }

            return result;
        }
    }

    @Override
    public JsonNode executeQueryToJson(String query, Map<String, Object> params) throws SQLException {
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            LOGGER.info("executeQueryToJson stmt: {}", stmt);
            setParams(stmt, params);
            try (ResultSet resultSet = stmt.executeQuery()) {
                return resultSetToJson(resultSet);
            }
        }
    }


    protected void setParams(PreparedStatement stmt, Map<String, Object> params) throws SQLException {
        LOGGER.info("params: {}", params);
        int index = 1;
        for (Object value : params.values()) {
            stmt.setObject(index++, value);
        }
    }

    protected void setParamByNames(PreparedStatement stmt, Map<String, Object> params) throws SQLException {
        LOGGER.info("params: {}", params);

        // Tạo một danh sách các tham số có tên trong câu lệnh SQL
        // Ví dụ: "SELECT * FROM k_metadata WHERE table_name = :table_name AND column_name = :column_name"
        String[] paramNames = getParamNamesFromSql(stmt.toString());

        int index = 1;
        for (String paramName : paramNames) {
            if (params.containsKey(paramName)) {
                stmt.setObject(index++, params.get(paramName));
            } else {
                throw new SQLException("Tham số '" + paramName + "' không được cung cấp");
            }
        }
    }

    private String[] getParamNamesFromSql(String sql) {
        // Sử dụng regex để tìm các tham số có tên trong câu lệnh SQL
        Pattern pattern = Pattern.compile(":([a-zA-Z_][a-zA-Z_0-9]*)");
        Matcher matcher = pattern.matcher(sql);

        List<String> paramNames = new ArrayList<>();
        while (matcher.find()) {
            paramNames.add(matcher.group(1)); // Lấy tên tham số (không bao gồm dấu ":")
        }

        return paramNames.toArray(new String[0]);
    }

    private String buildProcedureCall(String procedure, int paramCount) {
        StringBuilder callSQL = new StringBuilder("{ call ").append(procedure).append("(");
        callSQL.append("?,".repeat(Math.max(0, paramCount - 1)));
        if (paramCount > 0) callSQL.append("?");
        callSQL.append(") }");
        return callSQL.toString();
    }

    protected JsonNode resultSetToJson(ResultSet resultSet) throws SQLException {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);

                // Kiểm tra nếu giá trị là một chuỗi JSON
                if (value instanceof String) {
                    String stringValue = (String) value;
                    if (stringValue.startsWith("{") || stringValue.startsWith("[")) {
                        try {
                            JsonNode jsonValue = objectMapper.readTree(stringValue);
                            row.put(columnName, jsonValue);
                        } catch (JsonProcessingException e) {
                            LOGGER.error("Lỗi parse JSON cho cột {}", columnName, e);
                            row.put(columnName, value); // Giữ nguyên giá trị chuỗi nếu không thể parse
                        }
                    } else {
                        row.put(columnName, value);
                    }
                } else {
                    row.put(columnName, value);
                }
            }
            arrayNode.add(objectMapper.valueToTree(row));
        }
        return arrayNode;
    }


    protected JsonNode resultSetToJson_old(ResultSet resultSet) throws SQLException {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            arrayNode.add(objectMapper.valueToTree(row));
        }
        return arrayNode;
    }


    protected List<Map<String, Object>> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }

    // Generate cache key based on SQL and params
    private String generateCacheKey(String sql, Map<String, Object> params) {
        return sql + params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
    }

    protected abstract CallableStatement prepareProcedureStatement(Connection conn, String procedure, Map<String, Object> params, boolean hasOutCursor, boolean hasOutputParam) throws SQLException;

}
