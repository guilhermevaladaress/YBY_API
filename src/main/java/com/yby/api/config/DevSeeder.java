package com.yby.api.config;

import com.yby.api.entity.Usuario;
import com.yby.api.entity.enums.Role;
import com.yby.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seed opcional de usuarios para desenvolvimento/teste manual de endpoints.
 *
 * <p>Desabilitado por padrao. Habilite com {@code app.dev.seed-enabled=true} (nunca em
 * producao). So insere quando a tabela de usuarios esta vazia, preservando RN-012 (BCrypt)
 * e RN-002/RN-003 (perfis GESTOR/SERVIDOR). As senhas abaixo sao apenas para ambiente local.</p>
 */
@Component
@ConditionalOnProperty(name = "app.dev.seed-enabled", havingValue = "true")
public class DevSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DevSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        criar("Gestor TO", "gestor.to@yby.local", "gestor123", Role.GESTOR);
        criar("Servidor TO", "servidor.to@yby.local", "servidor123", Role.SERVIDOR);
        log.warn("DevSeeder ATIVO: usuarios de teste criados (gestor.to@yby.local / servidor.to@yby.local). "
            + "Nao use este perfil em producao.");
    }

    private void criar(String nome, String email, String senha, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setAtivo(true);
        usuario.setPrimeiroAcessoTrocaSenha(false);
        usuarioRepository.save(usuario);
    }
}
