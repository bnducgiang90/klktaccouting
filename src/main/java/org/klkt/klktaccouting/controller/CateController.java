package org.klkt.klktaccouting.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.service.spi.ServiceException;
import org.klkt.klktaccouting.service.CateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class CateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final CateService cateService;

    @PostMapping("/search")
    public ResponseEntity<?> get_list_cate_search(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> user_info = (Map<String, Object>)request.getAttribute("user");
            data.put("user", user_info);
            LOGGER.info("data: {}", data);

            List<Map<String, Object>> rs = this.cateService.get_list_cate_search(data);

            // Tạo response data theo format yêu cầu
            Map<String, Object> responseData = new HashMap<>();
            response.put("error_code", "000");
            response.put("error_message", "");
            if (!rs.isEmpty()) {
                responseData.put("total", rs.get(0).get("total"));
                responseData.put("items", rs);
            } else {
                responseData.put("total", 0);
                responseData.put("items", new ArrayList());
            }

            response.put("data", responseData);

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOGGER.error("Controller error: get_list_cate_search failed", e);

            response.put("error_code", "001");
            response.put("error_message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            LOGGER.error("Unexpected error: get_list_cate_search failed", e);

            response.put("error_code", "500");
            response.put("error_message", "System busy, please try again!!!, Internal server error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/get-list")
    public ResponseEntity<?> get_list_cate_data(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> user_info = (Map<String, Object>)request.getAttribute("user");
            data.put("user", user_info);
            LOGGER.info("data: {}", data);

            List<Map<String, Object>> rs = this.cateService.get_list_cate_data(data);

            // Tạo response data theo format yêu cầu
            Map<String, Object> responseData = new HashMap<>();
            response.put("error_code", "000");
            response.put("error_message", "");
            if (!rs.isEmpty()) {
                responseData.put("total", rs.get(0).get("total"));
                responseData.put("items", rs);
            } else {
                responseData.put("total", 0);
                responseData.put("items", new ArrayList());
            }

            response.put("data", responseData);

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOGGER.error("Controller error: get_list_cate_data failed", e);

            response.put("error_code", "001");
            response.put("error_message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            LOGGER.error("Unexpected error: get_list_cate_data failed", e);

            response.put("error_code", "500");
            response.put("error_message", "System busy, please try again!!!, Internal server error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsert_cate(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> user_info = (Map<String, Object>)request.getAttribute("user");
            data.put("user", user_info);
            LOGGER.info("data: {}", data);
            Map<String, Object> rs = this.cateService.upsert_cate(data);
            Map<String, Object> response = new HashMap<>();
            response.put("errorCode", "000");
            response.put("errorMessage", "");
            response.put("data", rs.get("hdr_id"));
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            LOGGER.error("Controller error: upsert_cate failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error: upsert_cate failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System busy, please try again!!!, Internal server error");
        }
    }

    @PostMapping("/update-status")
    public ResponseEntity<?> update_status(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> user_info = (Map<String, Object>)request.getAttribute("user");
            data.put("user", user_info);
            LOGGER.info("data: {}", data);
            Map<String, Object> rs = this.cateService.update_status(data);
            Map<String, Object> response = new HashMap<>();
            response.put("errorCode", "000");
            response.put("errorMessage", "");
            response.put("data", rs.get("hdr_id"));
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            LOGGER.error("Controller error: update_status failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error: update_status failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System busy, please try again!!!, Internal server error");
        }
    }


}
