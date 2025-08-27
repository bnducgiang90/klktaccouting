package org.klkt.klktaccouting.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.klkt.klktaccouting.core.database.rdbms.IDatabaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Supplier;

@DependsOn("databaseExecutorImpl")
@Repository
public class CateRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreRepository.class);
    private final Supplier<IDatabaseExecutor> businessDbExecutor;
    private final ObjectMapper objectMapper;

    private IDatabaseExecutor dbExecutor;

    @Autowired
    public CateRepository(Supplier<IDatabaseExecutor> businessDbExecutor, ObjectMapper objectMapper) {
        this.businessDbExecutor = businessDbExecutor;
        this.dbExecutor = this.businessDbExecutor.get();
        LOGGER.info("Using dbExecutor class: {}", this.dbExecutor.getClass().getName());
        this.objectMapper = objectMapper;
    }

    public synchronized IDatabaseExecutor getDbExecutor() {
        if (dbExecutor == null) {
            dbExecutor = businessDbExecutor.get();
        }
        return dbExecutor;
    }

    public List<Map<String, Object>> get_list_cate_data(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);
        List<Map<String, Object>> rs = this.getDbExecutor().executeProcedure(
                "sp_cate_get_list_data",
                params
        );

        return rs;
    }

    public List<Map<String, Object>> get_list_cate_search(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);
        List<Map<String, Object>> rs = this.getDbExecutor().executeProcedure(
                "sp_cate_search",
                params
        );

        return rs;
    }

    public Map<String, Object> upsert_cate(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);

        Map<String, Object> rs = this.getDbExecutor().executeProcedureWithOutputParams(
                "sp_cate_upsert",
                params
        );
        // Trả về một List chứa Map báo thành công
        Map<String, Object> result = new HashMap<>();
        result.put("out_data", rs.get("p_output"));

        return result;
    }

    public Map<String, Object> update_status(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);

        Map<String, Object> rs = this.getDbExecutor().executeProcedureWithOutputParams(
                "sp_cate_update_status",
                params
        );
        // Trả về một List chứa Map báo thành công
        Map<String, Object> result = new HashMap<>();
        result.put("out_data", rs.get("p_output"));

        return result;
    }

    public Map<String, Object> dmtaikhoan_update_balance(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);

        Map<String, Object> rs = this.getDbExecutor().executeProcedureWithOutputParams(
                "sp_tbldmtaikhoan_update_balance",
                params
        );
        // Trả về một List chứa Map báo thành công
        Map<String, Object> result = new HashMap<>();
        result.put("out_data", rs.get("p_output"));

        return result;
    }

    public List<Map<String, Object>> dmtaikhoan_chitiet_get_balance(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);
        List<Map<String, Object>> rs = this.getDbExecutor().executeProcedure(
                "sp_tbldmtaikhoan_chitiet_get_balance",
                params
        );

        return rs;
    }

    public Map<String, Object> update(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);

        Map<String, Object> rs = this.getDbExecutor().executeProcedureWithOutputParams(
                "sp_cate_update",
                params
        );
        // Trả về một List chứa Map báo thành công
        Map<String, Object> result = new HashMap<>();
        result.put("out_data", rs.get("p_output"));

        return result;
    }

}
