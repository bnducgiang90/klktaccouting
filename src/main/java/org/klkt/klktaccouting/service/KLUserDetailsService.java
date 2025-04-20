package org.klkt.klktaccouting.service;

import lombok.RequiredArgsConstructor;
import org.klkt.klktaccouting.repository.AuthRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KLUserDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        Map<String, Object> user;
        try {
            // Gọi repository để tìm user theo username hoặc email
            user = authRepository.getUserByUsername(Map.of("username", usernameOrEmail));
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with username/email: " + usernameOrEmail, e);
        }

        // Kiểm tra nếu user không tồn tại
        if (user == null || user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username/email: " + usernameOrEmail);
        }

        // Trả về UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.get("username").toString(),
                user.get("hash_password").toString(),
                Collections.emptyList() // Thay bằng roles nếu có
        );
    }
}