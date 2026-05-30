package com.yby.api.service;

import com.yby.api.config.AppSecurityProperties;
import com.yby.api.dto.ChangePasswordRequestDTO;
import com.yby.api.dto.LoginRequestDTO;
import com.yby.api.dto.LoginResponseDTO;
import com.yby.api.dto.UsuarioDTO;
import com.yby.api.entity.Usuario;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.UsuarioMapper;
import com.yby.api.repository.UsuarioRepository;
import com.yby.api.security.AppUserDetails;
import com.yby.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioMapper usuarioMapper;
    private final AppSecurityProperties appSecurityProperties;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UsuarioMapper usuarioMapper,
                       AppSecurityProperties appSecurityProperties) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.usuarioMapper = usuarioMapper;
        this.appSecurityProperties = appSecurityProperties;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.senha()));

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (!usuario.isAtivo()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Usuario desativado");
        }

        AppUserDetails details = new AppUserDetails(usuario);
        String token = jwtService.generateToken(details);
        return new LoginResponseDTO(
            "Bearer",
            token,
            appSecurityProperties.jwt().expiresInSeconds(),
            usuario.getRole().name()
        );
    }

    public UsuarioDTO me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        return usuarioMapper.toDTO(usuario);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Senha atual invalida");
        }
        if (request.senhaAtual().equals(request.novaSenha())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "A nova senha deve ser diferente da atual");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuario.setPrimeiroAcessoTrocaSenha(false);
        usuarioRepository.save(usuario);
    }
}
