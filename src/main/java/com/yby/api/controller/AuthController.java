package com.yby.api.controller;

import com.yby.api.dto.AuthUserDTO;
import com.yby.api.dto.LoginRequestDTO;
import com.yby.api.dto.LoginResponseDTO;
import com.yby.api.dto.PasswordChangeRequestDTO;
import com.yby.api.dto.RegisterRequestDTO;
import com.yby.api.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthUserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserDTO> me(Principal principal) {
        return ResponseEntity.ok(authService.me(principal.getName()));
    }

    @PatchMapping("/senha")
    public ResponseEntity<Void> changePassword(
        Principal principal,
        @Valid @RequestBody PasswordChangeRequestDTO request
    ) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
