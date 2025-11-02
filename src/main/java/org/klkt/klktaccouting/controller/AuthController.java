package org.klkt.klktaccouting.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.service.spi.ServiceException;
import org.klkt.klktaccouting.service.AuthService;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final CateService cateService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> rs = authService.login(request);
            return ResponseEntity.ok(rs);
        }
        catch (Exception e) {
           return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e));
//           return ResponseEntity.ok(Map.of("error", e));
        }
    }

    @PostMapping("/years")
    public ResponseEntity<Map<String, Object>> get_fiscal_year(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();

        try {
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
            LOGGER.error("Controller error: get_fiscal_year failed", e);

            response.put("error_code", "001");
            response.put("error_message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            LOGGER.error("Unexpected error: get_fiscal_year failed", e);

            response.put("error_code", "500");
            response.put("error_message", "System busy, please try again!!!, Internal server error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, Object> request) throws Exception {
        Map<String, Object> rs = authService.refreshToken(request);
        return ResponseEntity.ok(rs);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> rs = authService.logout(request);
            return ResponseEntity.ok(rs);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, Object> request) {
        authService.register(request);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

}
