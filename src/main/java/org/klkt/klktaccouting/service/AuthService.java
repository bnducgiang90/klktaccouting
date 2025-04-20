package org.klkt.klktaccouting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.klkt.klktaccouting.repository.AuthRepository;
import org.klkt.klktaccouting.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final AuthRepository authRepository;

    public Map<String, Object> login(Map<String, Object> data) throws Exception {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.get("username").toString(), data.get("password").toString())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Find user in database
            Map<String, Object> userDB = this.getUserByUsername(data);

            if (userDB == null) {
                throw new RuntimeException("User not found!!!");
            }

            // Generate tokens
            String accessToken = jwtUtils.generateToken(userDetails, userDB);
            String refreshToken = jwtUtils.generateRefreshToken(userDetails);

            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "tokenType", "Bearer",
                    "expiresIn", jwtUtils.getJwtExpirationMs(),
                    "user", Map.of(
                            "uId", userDB.get("u_id"),
                            "orgId", userDB.get("org_u_id"),
                            "email", userDB.get("email"),
                            "taxCode", userDB.get("tax_code")
                    )
            );
        } catch (BadCredentialsException e) {
            // Xử lý lỗi đăng nhập sai
//            return Map.of("error", "Invalid username or password");
            LOGGER.error("Invalid credentials: ", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Login error: ", e);
//            // Xử lý lỗi khác (ví dụ: user không tồn tại trong DB)
//            return Map.of("error", e.getMessage());
            throw e;
        }

    }

    public Map<String, Object> refreshToken(Map<String, Object> data) throws Exception {
        if (!jwtUtils.validateToken(data.get("refreshToken").toString())) {
            throw new Exception("Invalid refresh token");
        }

        String username = jwtUtils.extractUsername(data.get("refreshToken").toString());
        data.put("username", username);
        UserDetails userDetails = this.getUserDetails(data);

        // Find user in database
        Map<String, Object> userDB = this.getUserByUsername(data);

        if (userDB == null) {
            throw new RuntimeException("User not found!!!");
        }


        // Generate new tokens
        String newAccessToken = jwtUtils.generateToken(userDetails, userDB);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken,
                "tokenType", "Bearer",
                "expiresIn", jwtUtils.getJwtExpirationMs(),
                "user", Map.of(
                        "uId", userDB.get("u_id"),
                        "orgId", userDB.get("org_u_id"),
                        "email", userDB.get("email"),
                        "taxCode", userDB.get("tax_code")
                )
        );
    }

    public void register(Map<String, Object> data) {

        // Hash the password
        String hashedPassword = passwordEncoder.encode(data.get("password").toString());
        data.put("hash_password", hashedPassword);
        try {
            authRepository.createUser(data);
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    public Map<String, Object> getUserByUsername(Map<String, Object> data) throws Exception {
        Map<String, Object> user = authRepository.getUserByUsername(data);
        return user;
    }

    //    @Override
    public UserDetails getUserDetails(Map<String, Object> data) throws Exception {
        Map<String, Object> user = this.getUserByUsername(data);
        if (user != null) {
            return new org.springframework.security.core.userdetails.User(
                    user.get("username").toString(),
                    user.get("hash_password").toString(),
                    Collections.emptyList()
            );
        }
        return null;
    }
}
