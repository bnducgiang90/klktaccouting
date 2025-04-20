package org.klkt.klktaccouting.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.klkt.klktaccouting.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;


    // Danh sách các endpoint công khai (không cần token)
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/cate/**"
    );
    // Kiểm tra xem URI có thuộc danh sách công khai không
    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> new AntPathMatcher().match(pattern, requestURI));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException{

        String requestURI = request.getRequestURI();

        // Bỏ qua filter nếu request đến endpoint công khai
        if (isPublicEndpoint(requestURI)) {
            System.out.println("Not Need Authen!!!!");
            filterChain.doFilter(request, response);
            return;
        }


        try {
            String token = jwtUtils.getTokenFromRequest(request);
            System.out.println("TOKEN: " + token);

            // 1. Kiểm tra token có tồn tại không
            if (token == null || token.isBlank()) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token is missing");
                return;
            }

            // 2. Validate token
            if (!jwtUtils.validateToken(token)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "Invalid token");
                return;
            }

            // 3. Lấy thông tin user từ token
            String username = jwtUtils.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4. Kiểm tra token có hợp lệ với user không
            if (!jwtUtils.validateToken(token, userDetails)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token is invalid for user");
                return;
            }

            // 5. Thiết lập authentication
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token expired");
            return;
        } catch (Exception e) {
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}