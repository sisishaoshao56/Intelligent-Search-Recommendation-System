package com.csu.demo.demo.controller;

import com.csu.demo.demo.domain.User;
import com.csu.demo.demo.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        String token = loginService.register(user);
        return ResponseEntity.ok(buildTokenResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username,
                                                     @RequestParam String password) {
        String token = loginService.login(username, password);
        return ResponseEntity.ok(buildTokenResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        loginService.logout(token);
        return ResponseEntity.noContent().build();
    }

    private Map<String, String> buildTokenResponse(String token) {
        Map<String, String> body = new HashMap<>(1);
        body.put("token", token);
        return body;
    }
}
