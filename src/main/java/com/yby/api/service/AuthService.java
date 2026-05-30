package com.yby.api.service;

import com.yby.api.dto.AuthUserDTO;
import com.yby.api.dto.LoginRequestDTO;
import com.yby.api.dto.LoginResponseDTO;
import com.yby.api.dto.PasswordChangeRequestDTO;
import com.yby.api.dto.RegisterRequestDTO;
import com.yby.api.entity.UserAccount;
import com.yby.api.entity.enums.UserRole;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.AuthMapper;
import com.yby.api.repository.UserAccountRepository;
import com.yby.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        AuditService auditService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    @Transactional
    public AuthUserDTO register(RegisterRequestDTO request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Email ja cadastrado");
        }

        UserAccount user = new UserAccount();
        user.setNome(request.nome().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setSenhaHash(passwordEncoder.encode(request.senha()));
        user.setRole(request.role() == null ? UserRole.SERVIDOR : request.role());
        user.setAtivo(true);
        UserAccount saved = userAccountRepository.save(user);

        auditService.registrarEscritaGestor("REGISTER", "usuarios", String.valueOf(saved.getId()), saved.getEmail());
        return AuthMapper.toDto(saved);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.senha())
        );

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        String token = jwtService.generateToken(user);
        return new LoginResponseDTO("Bearer", token, jwtService.getExpirationSeconds(), AuthMapper.toDto(user));
    }

    public AuthUserDTO me(String email) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        return AuthMapper.toDto(user);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequestDTO request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(request.senhaAtual(), user.getSenhaHash())) {
            throw new BusinessException("Senha atual invalida");
        }

        if (request.senhaAtual().equals(request.novaSenha())) {
            throw new BusinessException("A nova senha deve ser diferente da atual");
        }

        user.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        user.setTrocaSenhaPrimeiroAcesso(false);
        userAccountRepository.save(user);
        auditService.registrarEscritaGestor("CHANGE_PASSWORD", "usuarios", String.valueOf(user.getId()), user.getEmail());
    }
}
