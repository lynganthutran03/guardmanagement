package com.lytran.guardmanagement.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lytran.guardmanagement.dto.LoginRequest;
import com.lytran.guardmanagement.dto.LoginResponse;
import com.lytran.guardmanagement.entity.User;
import com.lytran.guardmanagement.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return new LoginResponse(true, "Login successful", user.getFullName(), user.getRole());
            } else {
                return new LoginResponse(false, "Incorrect password", null, null);
            }
        } else {
            return new LoginResponse(false, "User not found", null, null);
        }
    }
}
