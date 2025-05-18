package org.klkt.klktaccouting.controller;


import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.klkt.klktaccouting.service.KLKTCateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/cate")
public class KLKTCateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(KLKTCateController.class);
    private final KLKTCateService klktCateService;

    @Autowired
    public KLKTCateController(KLKTCateService klktCateService) {
        this.klktCateService = klktCateService;
    }

    @Operation(summary = "Lấy thông tin người dùng")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ")
    })
    @GetMapping("/user")
    public String getUser() {
        return "Thông tin người dùng";
    }

    @GetMapping("/metadata/{tableName}")
    public ResponseEntity<Map<String, Object>> getMetadata(@PathVariable String tableName) {
        try {
            Map<String, Object> response = new HashMap<>();
            JsonNode metadataMap = klktCateService.getMetadataByTableName(tableName);

            // Thêm metadata vào response
            response.put("metadata", metadataMap);

            // Lấy dropdown list:
            Map<String, List<Map<String, Object>>> dropdownValues
                    = klktCateService.getDropdownDataFromTable(tableName);
            response.put("dropdownValues", dropdownValues);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("ERROR: {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage(), "details", e.getStackTrace()));
        }
    }

    @GetMapping("/get-tables")
    @Operation(summary = "Get metadata all tables")
    @ApiResponse(
            content = @Content(schema = @Schema(implementation = Object.class))
    )
    public ResponseEntity<JsonNode> getAllTables() {
        try {
            JsonNode tableMap = klktCateService.getAllTables();
            LOGGER.info("tableMap: ", tableMap);
            return ResponseEntity.ok(tableMap);
        } catch (Exception e) {
            LOGGER.error("ERROR:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((JsonNode) Map.of("error", e.getMessage()));
        }
    }

    // Tạo mới record
    @PostMapping("/create/{tableName}")
    public ResponseEntity<String> createRecord(@PathVariable String tableName, @RequestBody Map<String, Object> data) {
        try {
            int result = klktCateService.createRecord(tableName, data);
            return result > 0 ? ResponseEntity.ok("Created") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()).toString());
        }
    }

    @PostMapping("/search/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> searchRecords(@PathVariable String tableName, @RequestBody Map<String, String> conditions) {
        try {
            String query = conditions.get("query");
            List<Map<String, Object>> results = klktCateService.searchRecords(tableName, query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            LOGGER.error("ERROR: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((List<Map<String, Object>>) Map.of("error", e.getMessage()));
        }
    }

    // Lấy tất cả record
    @GetMapping("/get-all/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> getAllRecords(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> results = klktCateService.getAllRecords(tableName);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((List<Map<String, Object>>) Map.of("error", e.getMessage()));
        }
    }

    // Update record
    @PutMapping("/update/{tableName}/{id}")
    @Operation(summary = "Update record")
    public ResponseEntity<String> updateRecord(@PathVariable String tableName, @PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            LOGGER.info("Updating record: {}", tableName);
            int result = klktCateService.updateRecord(tableName, data, id);
            return result > 0 ? ResponseEntity.ok("Updated") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()).toString());
        }
    }

    @PutMapping("/update2")
    public ResponseEntity<String> updateRecord2(@RequestBody Map<String, Object> data) {
        try {
            int result = 1;
            return result > 0 ? ResponseEntity.ok("Updated") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()).toString());
        }
    }

    // Cập nhật bản ghi
    @PutMapping("/edit/{tableName}")
    public ResponseEntity<String> updateRecord(@PathVariable String tableName, @RequestBody Map<String, Object> recordData) {
        try {
            int result = klktCateService.updateRecord(tableName, recordData);
            return result > 0 ? ResponseEntity.ok("Updated") : ResponseEntity.ok("No match data update!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()).toString());
        }
    }

    // Xóa record
    @DeleteMapping("/delete/{tableName}/{id}")
    public ResponseEntity<String> deleteRecord(@PathVariable String tableName, @PathVariable Long id) {
        try {
            int result = klktCateService.deleteRecord(tableName, id);
            return result > 0 ? ResponseEntity.ok("Deleted") : ResponseEntity.ok("No match data delete!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()).toString());
        }
    }

    // Xóa bản ghi
    @DeleteMapping("/remove/{tableName}")
    public ResponseEntity<String> deleteRecord(
            @PathVariable String tableName,
            @RequestBody Map<String, Object> primaryKeyValues) {
        try {
            int result = klktCateService.deleteRecord(tableName, primaryKeyValues);
            return result > 0 ? ResponseEntity.ok("Deleted") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()).toString());
        }
    }

}
