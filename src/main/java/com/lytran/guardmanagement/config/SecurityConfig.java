package com.lytran.guardmanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.lytran.guardmanagement.security.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/login").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/shifts/**",
                                            "/api/leave-requests/request",
                                            "/api/leave-requests/history",
                                            "/api/attendance/check-in").hasRole("GUARD")
                .requestMatchers("/api/guards",
                                            "/api/manager/**",
                                            "/api/leave-requests/pending",
                                            "/api/leave-requests/approve/**",
                                            "/api/leave-requests/deny/**",
                                            "/api/leave-requests/approved").hasRole("MANAGER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/admin/locations", "/api/admin/timeslots").authenticated()
                .anyRequest().authenticated()
                .and()
                .authenticationProvider(authProvider())
                .formLogin()
                .loginProcessingUrl("/api/login")
                .successHandler((req, res, auth) -> {
                    res.setContentType("application/json");
                    res.getWriter().write("{\"success\":true}");
                })
                .failureHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        return http.build();
    }
}
