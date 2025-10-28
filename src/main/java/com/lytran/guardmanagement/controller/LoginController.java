package com.lytran.guardmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.LoginRequest;
import com.lytran.guardmanagement.dto.LoginResponse;
import com.lytran.guardmanagement.entity.Role;
import com.lytran.guardmanagement.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            UsernamePasswordAuthenticationToken authRequest
                    = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

            Authentication auth = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession(true);  // Create session

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

            String fullName;
            Role role;

            if (userDetails.isGuard()) {
                fullName = userDetails.getGuard().getFullName();
                role = userDetails.getGuard().getRole();
            } else {
                fullName = userDetails.getManager().getFullName();
                role = userDetails.getManager().getRole();
            }

            return ResponseEntity.ok(new LoginResponse(true, "Login successful", fullName, role));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Invalid username or password", null, null));
        }
    }
}
