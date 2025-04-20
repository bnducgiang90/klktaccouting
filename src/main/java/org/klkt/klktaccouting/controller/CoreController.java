package org.klkt.klktaccouting.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.service.spi.ServiceException;
import org.klkt.klktaccouting.service.CoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class CoreController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final CoreService coreService;

    @PostMapping("/get-list-data")
    public ResponseEntity<?> get_list_data_by_user(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> user_info = (Map<String, Object>)request.getAttribute("user");
            data.put("user", user_info);
            LOGGER.info("data: {}", data);
            List<Map<String, Object>> rs = this.coreService.get_list_data_by_user(data);
            return ResponseEntity.ok(rs);
        } catch (ServiceException e) {
            LOGGER.error("Controller error: get_list_data_by_user failed", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error: get_list_data_by_user failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System busy, please try again!!!, Internal server error");
        }
    }

}
