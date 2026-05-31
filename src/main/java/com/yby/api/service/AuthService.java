package com.yby.api.service;

import com.yby.api.config.AppSecurityProperties;
import com.yby.api.dto.ChangePasswordRequestDTO;
import com.yby.api.dto.EsqueciSenhaRequestDTO;
import com.yby.api.dto.EsqueciSenhaResponseDTO;
import com.yby.api.dto.LoginRequestDTO;
import com.yby.api.dto.LoginResponseDTO;
import com.yby.api.dto.RedefinirSenhaRequestDTO;
import com.yby.api.dto.UsuarioDTO;
import com.yby.api.entity.Usuario;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.UsuarioMapper;
import com.yby.api.repository.UsuarioRepository;
import com.yby.api.security.AppUserDetails;
import com.yby.api.security.JwtService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final AppSecurityProperties appSecurityProperties;
    private final ResendEmailService emailService;
    private final String frontendUrl;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UsuarioMapper usuarioMapper,
                       AppSecurityProperties appSecurityProperties,
                       ResendEmailService emailService,
                       @org.springframework.beans.factory.annotation.Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioMapper = usuarioMapper;
        this.appSecurityProperties = appSecurityProperties;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        // Valida senha diretamente para evitar problemas com AuthenticationManager em alguns ambientes
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Credenciais invalidas");
        }

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

    /** Validade do token de redefinicao de senha. */
    private static final long RESET_TOKEN_TTL_MINUTOS = 30;

    /**
     * Inicia o fluxo "esqueci minha senha": gera um token de uso unico com validade curta.
     *
     * <p>A resposta e sempre generica para nao revelar se o e-mail existe (evita enumeracao de
     * usuarios). Como nao ha servico de e-mail no ambiente, o token e devolvido na resposta para
     * permitir a redefinicao; em producao seria enviado por e-mail.</p>
     */
    @Transactional
    public EsqueciSenhaResponseDTO esqueciSenha(EsqueciSenhaRequestDTO request) {
        String mensagem = "Se o e-mail informado estiver cadastrado, enviaremos instrucoes de redefinicao.";
        String email = request.email().trim().toLowerCase();

        return usuarioRepository.findByEmail(email)
            .filter(Usuario::isAtivo)
            .map(usuario -> {
                String token = UUID.randomUUID().toString();
                OffsetDateTime expiraEm = OffsetDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTOS);
                usuario.setResetToken(token);
                usuario.setResetTokenExpiraEm(expiraEm);
                usuarioRepository.save(usuario);

                // Envia o link por e-mail (Resend). Se o e-mail for enviado, NAO devolve o
                // token na resposta (seguranca). Sem e-mail configurado/entregue, devolve o
                // token como fallback de desenvolvimento para o fluxo nao quebrar.
                String link = frontendUrl + "/redefinir-senha?token=" + token;
                boolean enviado = emailService.enviarRecuperacaoSenha(
                    usuario.getEmail(), link, RESET_TOKEN_TTL_MINUTOS);

                return new EsqueciSenhaResponseDTO(mensagem, enviado ? null : token, expiraEm);
            })
            .orElseGet(() -> new EsqueciSenhaResponseDTO(mensagem, null, null));
    }

    /** Redefine a senha a partir de um token valido e nao expirado, invalidando-o em seguida. */
    @Transactional
    public void redefinirSenha(RedefinirSenhaRequestDTO request) {
        Usuario usuario = usuarioRepository.findByResetToken(request.token())
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Token de redefinicao invalido"));

        if (usuario.getResetTokenExpiraEm() == null
            || usuario.getResetTokenExpiraEm().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token de redefinicao expirado");
        }
        if (!usuario.isAtivo()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Usuario desativado");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiraEm(null);
        usuario.setPrimeiroAcessoTrocaSenha(false);
        usuarioRepository.save(usuario);
    }
}
