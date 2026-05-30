package com.yby.api.controller;

import com.yby.api.dto.ChangePasswordRequestDTO;
import com.yby.api.dto.EsqueciSenhaRequestDTO;
import com.yby.api.dto.EsqueciSenhaResponseDTO;
import com.yby.api.dto.LoginRequestDTO;
import com.yby.api.dto.LoginResponseDTO;
import com.yby.api.dto.RedefinirSenhaRequestDTO;
import com.yby.api.dto.UsuarioDTO;
import com.yby.api.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<EsqueciSenhaResponseDTO> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequestDTO request) {
        return ResponseEntity.ok(authService.esqueciSenha(request));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO request) {
        authService.redefinirSenha(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me(Principal principal) {
        return ResponseEntity.ok(authService.me(principal.getName()));
    }

    @PatchMapping("/senha")
    public ResponseEntity<Void> changePassword(Principal principal,
                                               @Valid @RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
