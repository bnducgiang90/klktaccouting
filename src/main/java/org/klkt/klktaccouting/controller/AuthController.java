package org.klkt.klktaccouting.controller;


import org.klkt.klktaccouting.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

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
