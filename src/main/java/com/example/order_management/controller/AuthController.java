package com.example.order_management.controller;

import com.example.order_management.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username) {
        String token = jwtUtils.generateToken(username);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = jwtUtils.getTokenFromHeader(authHeader);
        Map<String, Object> response = new HashMap<>();
        
        if (token != null && jwtUtils.validateToken(token)) {
            response.put("valid", true);
            response.put("username", jwtUtils.getUsernameFromToken(token));
            return ResponseEntity.ok(response);
        }
        
        response.put("valid", false);
        return ResponseEntity.badRequest().body(response);
    }
}
