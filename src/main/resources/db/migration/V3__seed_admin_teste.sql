-- V3 - Usuario GESTOR de teste com senha conhecida para uso manual da API (Swagger/Postman).
-- Credenciais de DESENVOLVIMENTO/TESTE (trocar/remover em producao):
--   email: admin.teste@yby.local
--   senha: Gestor@123
-- Hash BCrypt gerado com BCryptPasswordEncoder (cost 10).
INSERT INTO usuarios (nome, email, senha_hash, role, ativo, primeiro_acesso_troca_senha, created_at, updated_at)
VALUES (
    'Admin Teste',
    'admin.teste@yby.local',
    '$2a$10$DNp6C5UWLiOJw.u1etTnve6MEyvFHfDxuT.jJAXF6I6DZ1zwr4Fa.',
    'GESTOR',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
