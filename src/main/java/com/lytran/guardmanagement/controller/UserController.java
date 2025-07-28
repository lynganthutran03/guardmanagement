package com.lytran.guardmanagement.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.entity.Guard;
import com.lytran.guardmanagement.entity.Manager;
import com.lytran.guardmanagement.security.CustomUserDetails;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/userinfo")
    public Map<String, Object> getUserInfo(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        if (user.isGuard()) {
            Guard guardEntity = user.getGuard();
            return Map.of(
                "username", guardEntity.getUsername(),
                "fullName", guardEntity.getFullName(),
                "identityNumber", guardEntity.getIdentityNumber(),
                "role", "GUARD"
            );
        } else {
            Manager managerEntity = user.getManager();
            return Map.of(
                "username", managerEntity.getUsername(),
                "fullName", managerEntity.getFullName(),
                "identityNumber", managerEntity.getIdentityNumber(),
                "role", "MANAGER"
            );
        }
    }
}
