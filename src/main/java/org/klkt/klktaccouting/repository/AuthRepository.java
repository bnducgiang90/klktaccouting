package org.klkt.klktaccouting.repository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AuthRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(KLKTCateService.class);
    private final Supplier<IDatabaseExecutor> businessDbExecutor;

    private IDatabaseExecutor dbExecutor;

    @Autowired
    public AuthRepository(Supplier<IDatabaseExecutor> businessDbExecutor) {
        this.businessDbExecutor = businessDbExecutor;
        this.dbExecutor = this.businessDbExecutor.get();
        LOGGER.info("Using dbExecutor class: {}", this.dbExecutor.getClass().getName());
    }

    public synchronized IDatabaseExecutor getDbExecutor() {
        if (dbExecutor == null) {
            dbExecutor = businessDbExecutor.get();
        }
        return dbExecutor;
    }

    public void createUser(Map<String, Object> data) throws Exception {

        String json = new ObjectMapper().writeValueAsString(data);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);
        this.getDbExecutor().executeProcedureNonQuery("sp_users_insert", params);
    }

    public Map<String, Object> getUserByUsername(Map<String, Object> data) throws Exception {
        String json = new ObjectMapper().writeValueAsString(data);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_params", json);
        List<Map<String, Object>> rs = this.getDbExecutor().executeProcedure("sp_users_get_by_username", params);
        if (rs!=null && rs.size()>0)
            return rs.get(0);
        return null;
    }

}
