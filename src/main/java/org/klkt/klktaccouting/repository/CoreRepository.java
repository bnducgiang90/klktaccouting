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
public class CoreRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreRepository.class);
    private final Supplier<IDatabaseExecutor> businessDbExecutor;
    private final ObjectMapper objectMapper;

    private IDatabaseExecutor dbExecutor;

    @Autowired
    public CoreRepository(Supplier<IDatabaseExecutor> businessDbExecutor, ObjectMapper objectMapper) {
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

    public List<Map<String, Object>> get_list_data_by_user(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);
        List<Map<String, Object>> rs = this.getDbExecutor().executeProcedure(
                "sp_core_get_list_data_by_user",
                params
        );

        return rs;
    }

    public Map<String, Object> upsert_tax_doc(Map<String, Object> data) throws Exception {
        String json = this.objectMapper.writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);

        Map<String, Object> rs = this.getDbExecutor().executeProcedureWithOutputParams(
                "sp_core_insert_tax_docs_by_user_output",
                params
        );
        //Map.of("p_params", json)

        // Trả về một List chứa Map báo thành công
        Map<String, Object> result = new HashMap<>();
        result.put("hdr_id", rs.get("p_output"));

        return result;
    }





}
