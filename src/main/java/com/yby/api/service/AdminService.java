package com.yby.api.service;

import com.yby.api.dto.CreateUsuarioDTO;
import com.yby.api.dto.RelatorioDTO;
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
        usuario.setPrimeiroAcessoTrocaSenha(true);

        String senhaInicial = gerarSenhaInicial();
        usuario.setSenhaHash(passwordEncoder.encode(senhaInicial));

        Usuario saved = usuarioRepository.save(usuario);
        auditService.registrarEscritaGestor("CREATE", "usuarios", String.valueOf(saved.getId()), dto);

        // Em ambiente produtivo o envio da senha inicial deve ocorrer em canal seguro externo.
        return usuarioMapper.toDTO(saved);
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
