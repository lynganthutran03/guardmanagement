package com.lytran.guardmanagement.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.entity.User;
import com.lytran.guardmanagement.security.CustomUserDetails;

@RestController
@RequestMapping("/api")
public class UserController {
    @GetMapping("/userinfo")
    public Map<String, Object> getUserInfo(Authentication auth) { 
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        User userEntity = user.getUser();
        return Map.of(
            "username", userEntity.getUsername(),
            "fullName", userEntity.getFullName(),
            "role", userEntity.getRole().toString()
        );
    }
}
