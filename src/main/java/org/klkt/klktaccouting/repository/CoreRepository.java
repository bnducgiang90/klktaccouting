package org.klkt.klktaccouting.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.klkt.klktaccouting.core.database.rdbms.IDatabaseExecutor;
import org.klkt.klktaccouting.service.KLKTCateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<Map<String, Object>> rs = this.getDbExecutor().executeProcedure(
                "sp_core_get_list_data_by_user",
                Map.of("p_params", json)
        );

        return rs;
    }

}
