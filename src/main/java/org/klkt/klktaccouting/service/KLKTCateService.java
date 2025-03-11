package org.klkt.klktaccouting.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.klkt.klktaccouting.core.database.rdbms.IDatabaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@DependsOn("databaseExecutorImpl")
@Service
public class KLKTCateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KLKTCateService.class);
    private final Supplier<IDatabaseExecutor> businessDbExecutor;

    private IDatabaseExecutor dbExecutor;

    @Autowired
    public KLKTCateService(Supplier<IDatabaseExecutor> businessDbExecutor) {
        this.businessDbExecutor = businessDbExecutor;
        this.dbExecutor = this.businessDbExecutor.get();
    }

    public synchronized IDatabaseExecutor getDbExecutor() {
        if (dbExecutor == null) {
            dbExecutor = businessDbExecutor.get();
        }
        return dbExecutor;
    }

    public JsonNode getMetadataByTableName(String tableName) throws SQLException {
        JsonNode metadatas = this.dbExecutor.executeQueryToJson(
                "SELECT * FROM k_metadata where table_name= ?",
                Map.of("table_name", tableName));

        return metadatas.isEmpty() ? null : metadatas.get(0);
    }

    public JsonNode getAllTables() throws SQLException {
        JsonNode jsonNode = this.dbExecutor.executeQueryToJson("select * from k_metadata", null);
        return jsonNode;
    }

    public Map<String, List<Map<String, Object>>> getDropdownDataFromTable(String tableName)
            throws Exception {

        JsonNode metadata = this.getMetadataByTableName(tableName);

        // Parse searchable_columns từ cột JSON
        List<Map<String, Object>> dropDownColumns =
                new ObjectMapper().readValue(metadata.get("column_dropdown_sources").toString(), List.class);

        // Lấy giá trị dropdown từ các bảng khác nhau
        Map<String, List<Map<String, Object>>> dropdownValues = new HashMap<>();
        for (Map<String, Object> column : dropDownColumns) {
            if ("dropdown".equals(column.get("type"))) {
                String sourceTable = (String) column.get("source_table");
                String valueColumn = (String) column.get("value_column");
                String labelColumn = (String) column.get("label_column");
                String query = String.format("SELECT %s as id, %s as name FROM %s", valueColumn, labelColumn, sourceTable);
                List<Map<String, Object>> dropdownData = this.dbExecutor.executeQuery(query, null);
                dropdownValues.put((String) column.get("name"), dropdownData);
            }
        }

        return dropdownValues;
    }

    public List<Map<String, Object>> getDropdownDataFromTable(String tableName, String valueColumn, String labelColumn)
            throws SQLException {
        String query = String.format("SELECT %s, %s FROM %s", valueColumn, labelColumn, tableName);
        return this.dbExecutor.executeQuery(query, null);
    }

    public Map<String, List<Map<String, Object>>> getDropdownData(String tableName)
            throws Exception {
        JsonNode metadata = this.getMetadataByTableName(tableName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dropdownSources = objectMapper.readTree(metadata.get("column_dropdown_sources").toString());
        Map<String, List<Map<String, Object>>> dropdownValues = new HashMap<>();

        dropdownSources.fields().forEachRemaining(entry -> {
            String columnName = entry.getKey();
            String sourceTable = entry.getValue().asText();

            String query = "SELECT id, name FROM " + sourceTable;
            try {
                List<Map<String, Object>> values = this.dbExecutor.executeQuery(query, null);
                dropdownValues.put(columnName, values);
            } catch (SQLException e) {
                // Handle the exception or log it
                LOGGER.error("Error executing query for dropdown data: {}", e.getMessage(), e);
            }
        });

        return dropdownValues;
    }

    // Tạo mới record
    public int createRecord(String tableName, Map<String, Object> data) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
        queryBuilder.append(tableName).append(" (");
        StringBuilder valueBuilder = new StringBuilder("VALUES (");

        data.forEach((column, value) -> {
            queryBuilder.append(column).append(",");
            valueBuilder.append("'").append(value).append("',");
        });

        queryBuilder.setLength(queryBuilder.length() - 1);
        valueBuilder.setLength(valueBuilder.length() - 1);

        queryBuilder.append(") ").append(valueBuilder).append(")");
        return this.dbExecutor.executeNonQuery(queryBuilder.toString(), null);
    }

    // Hàm tìm kiếm dựa trên điều kiện động
    public List<Map<String, Object>> searchRecords(String tableName, Map<String, Object> conditions) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE 1=1");

        conditions.forEach((column, value) -> {
            queryBuilder.append(" AND ").append(column).append(" ILIKE '%").append(value).append("%'");
        });

        return this.dbExecutor.executeQuery(queryBuilder.toString(), null);
    }

    public List<Map<String, Object>> searchRecords(String tableName, String searchQuery)
            throws Exception {
        JsonNode metadata = this.getMetadataByTableName(tableName);
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> listColumnsSearch = objectMapper.readValue(metadata.get("column_searchs").toString(), new TypeReference<>() {
        });

        String sql = "SELECT * FROM " + tableName + " WHERE 1=1";
        Map<String, Object> params = new HashMap<>();

        if (listColumnsSearch != null && !listColumnsSearch.isEmpty()) {
            sql += " AND (";
            for (int i = 0; i < listColumnsSearch.size(); i++) {
                String column = listColumnsSearch.get(i);
                sql += column + " ILIKE ?" + i;

                if (i < listColumnsSearch.size() - 1) {
                    sql += " OR ";
                }
                params.put(column, "%" + searchQuery + "%");
            }
            sql += ")";
        }

        return this.dbExecutor.executeQuery(sql, params);
    }

    // Lấy tất cả record
    public List<Map<String, Object>> getAllRecords(String tableName) throws SQLException {
        return this.dbExecutor.executeQuery("SELECT * FROM " + tableName, null);
    }

    // Update record
    public int updateRecord(String tableName, Map<String, Object> data, Long id) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        data.forEach((column, value) -> {
            queryBuilder.append(column).append(" = '").append(value).append("',");
        });

        queryBuilder.setLength(queryBuilder.length() - 1);
        queryBuilder.append(" WHERE id = ?");
        return this.dbExecutor.executeNonQuery(queryBuilder.toString(), Map.of("id", id));
    }

    public int updateRecord(String tableName, Map<String, Object> data)
            throws Exception {
        JsonNode metadata = this.getMetadataByTableName(tableName);
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> primaryKeys
                = objectMapper.readValue(metadata.get("column_primarys").toString(), new TypeReference<>() {
        });
        // Xây dựng câu truy vấn `UPDATE`
        StringBuilder queryBuilder = new StringBuilder("UPDATE " + tableName + " SET ");
        Map<String, Object> params = new HashMap<>();

        // Thêm các cột cần cập nhật
        data.forEach((key, value) -> {
            if (key != "primaryKeys") {
                if (!primaryKeys.contains(key)) { // Bỏ qua primary keys
//                    queryBuilder.append(key).append(" = ?, ");
//                    params.add(value);
                    if (value !=null) {
                        queryBuilder.append(key).append(" = '").append(value).append("',");
                    } else {
                        queryBuilder.append(key).append(" = ").append("null").append(",");
                    }
                }
            }
        });
        // Xóa dấu phẩy cuối cùng và thêm WHERE cho primary keys
//        queryBuilder.deleteCharAt(queryBuilder.length() - 2);
        queryBuilder.setLength(queryBuilder.length() - 1);
        queryBuilder.append(" WHERE ");
        primaryKeys.forEach(primaryKey -> {
            queryBuilder.append(primaryKey).append(" = ? AND ");
            params.put(primaryKey, data.get(primaryKey));
        });

        // Xóa " AND " cuối cùng
        queryBuilder.delete(queryBuilder.length() - 5, queryBuilder.length());

        // Thực thi truy vấn
        int r = this.dbExecutor.executeNonQuery(queryBuilder.toString(), params);
        return r;
    }

    // Xóa record
    public int deleteRecord(String tableName, Long id) throws SQLException {
        String query = "DELETE FROM " + tableName + " WHERE id = ?";
        return this.dbExecutor.executeNonQuery(query, Map.of("id", id));
    }

    // Xóa record
    public int deleteRecord(String tableName, Map<String, Object> primaryKeyValues)
            throws Exception {
        JsonNode metadata = this.getMetadataByTableName(tableName);
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> primaryKeys
                = objectMapper.readValue(metadata.get("column_primarys").toString(), new TypeReference<>() {
        });

        // Xây dựng câu truy vấn `DELETE`
        StringBuilder queryBuilder = new StringBuilder("DELETE FROM " + tableName + " WHERE ");
        Map<String, Object> params = new HashMap<>();

        primaryKeys.forEach(primaryKey -> {
            queryBuilder.append(primaryKey).append(" = ? AND ");
            params.put(primaryKey, primaryKeyValues.get(primaryKey));
        });

        // Xóa " AND " cuối cùng
        queryBuilder.delete(queryBuilder.length() - 5, queryBuilder.length());

        // Thực thi truy vấn
        int r = this.dbExecutor.executeNonQuery(queryBuilder.toString(), params);

        return r;

    }
}
