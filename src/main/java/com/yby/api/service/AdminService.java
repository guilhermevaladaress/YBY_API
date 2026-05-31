package com.yby.api.service;

import com.yby.api.dto.CreateUsuarioDTO;
import com.yby.api.dto.RelatorioDTO;
import com.yby.api.dto.UpdateUsuarioDTO;
import com.yby.api.dto.UsuarioDTO;
import com.yby.api.dto.UsuarioStatusPatchDTO;
import com.yby.api.entity.Usuario;
import com.yby.api.entity.enums.Role;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ConflictException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.UsuarioMapper;
import com.yby.api.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final AuditService auditService;

    public AdminService(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        UsuarioMapper usuarioMapper,
                        AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
        this.auditService = auditService;
    }

    public Page<UsuarioDTO> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(usuarioMapper::toDTO);
    }

    @Transactional
    public UsuarioDTO criarUsuario(CreateUsuarioDTO dto) {
        String email = dto.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("Email ja cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome().trim());
        usuario.setEmail(email);
        usuario.setRole(parseRole(dto.role()));
        usuario.setAtivo(true);

        // Senha definida pelo gestor; se vier em branco, gera uma aleatoria.
        boolean senhaDefinida = dto.senha() != null && !dto.senha().isBlank();
        String senha = senhaDefinida ? dto.senha() : gerarSenhaInicial();
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        // Se o gestor ja definiu a senha, nao forca troca no primeiro acesso.
        usuario.setPrimeiroAcessoTrocaSenha(!senhaDefinida);

        Usuario saved = usuarioRepository.save(usuario);
        auditService.registrarEscritaGestor("CREATE", "usuarios", String.valueOf(saved.getId()), dto.email());

        return usuarioMapper.toDTO(saved);
    }

    @Transactional
    public UsuarioDTO atualizarUsuario(Long id, UpdateUsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        usuario.setNome(dto.nome().trim());
        usuario.setRole(parseRole(dto.role()));

        // Senha opcional: se informada, redefine; em branco mantem a atual.
        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
            usuario.setPrimeiroAcessoTrocaSenha(false);
        }

        Usuario saved = usuarioRepository.save(usuario);
        auditService.registrarEscritaGestor("UPDATE", "usuarios", String.valueOf(saved.getId()), dto.nome());
        return usuarioMapper.toDTO(saved);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        // Evita o gestor logado excluir a propria conta (lockout).
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && usuario.getEmail().equalsIgnoreCase(auth.getName())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Voce nao pode excluir a propria conta");
        }

        usuarioRepository.delete(usuario);
        auditService.registrarEscritaGestor("DELETE", "usuarios", String.valueOf(id), usuario.getEmail());
    }

    @Transactional
    public UsuarioDTO atualizarStatus(Long id, UsuarioStatusPatchDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        usuario.setAtivo(dto.ativo());
        Usuario saved = usuarioRepository.save(usuario);
        auditService.registrarEscritaGestor("PATCH_STATUS", "usuarios", String.valueOf(saved.getId()), dto);
        return usuarioMapper.toDTO(saved);
    }

    public RelatorioDTO gerarRelatorioExecutivo() {
        long totalUsuarios = usuarioRepository.count();
        String observacao = "Relatorio administrativo gerado. Usuarios cadastrados: " + totalUsuarios;
        return new RelatorioDTO("relatorio_admin", "administrativo", OffsetDateTime.now(), observacao);
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "role invalida");
        }
    }

    private String gerarSenhaInicial() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
