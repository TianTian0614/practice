package com.agriassist.config;

import com.agriassist.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/articles/**",
                                "/api/community/**", "/api/user/public/**", "/api/search/**",
                                "/api/ai/consult", "/swagger-ui.html", "/v3/api-docs/**",
                                "/api/recommend/**", "/api/point/rank").permitAll()
                        // 供需：只读列表与匹配允许匿名（公开浏览/大屏），写操作需登录
                        .requestMatchers(HttpMethod.GET, "/api/supply/list", "/api/supply/match/**").permitAll()
                        // 大屏/溯源/价格行情：只读 GET 匿名可看（大屏投屏场景），写操作仍需登录
                        .requestMatchers(HttpMethod.GET, "/api/stats/**", "/api/trace/**", "/api/price/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Component
    public static class JwtAuthFilter extends OncePerRequestFilter {
        private final JwtUtil jwtUtil;
        public JwtAuthFilter(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String h = request.getHeader("Authorization");
            if (h != null && h.startsWith("Bearer ")) {
                try {
                    Claims c = jwtUtil.parse(h.substring(7));
                    var auth = new UsernamePasswordAuthenticationToken(
                            Long.parseLong(c.getSubject()), null,
                            AuthorityUtils.createAuthorityList("ROLE_" + c.get("role")));
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ignored) {}
            }
            chain.doFilter(request, response);
        }
    }
}
