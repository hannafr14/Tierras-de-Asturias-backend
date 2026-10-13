package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.AuthResponseDto;
import com.tierrasdeasturias.backend.dtos.LoginRequestDto;
import com.tierrasdeasturias.backend.dtos.RegisterRequestDto;
import com.tierrasdeasturias.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }
}