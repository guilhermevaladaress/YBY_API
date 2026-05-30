-- Seed inicial para o banco (V2)
-- Insere usuários e municípios básicos, e em seguida dados de emissões e indicadores.

-- Insert usuarios (ignora se já existir email)
INSERT INTO usuarios (nome, email, senha_hash, role, ativo, troca_senha_primeiro_acesso, created_at, updated_at)
VALUES
  ('Gestor TO', 'gestor.to@yby.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiXU5dJ3RvjoMnIKh4j/wQUp3NEPoPF', 'GESTOR', TRUE, FALSE, NOW(), NOW()),
  ('Servidor TO', 'servidor.to@yby.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiXU5dJ3RvjoMnIKh4j/wQUp3NEPoPF', 'SERVIDOR', TRUE, FALSE, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Insert municipios (parte reduzida para evitar arquivo muito grande; você pode manter o data.sql original em db/migration se preferir)
INSERT INTO municipios (nome, codigo_ibge, area_ha, score_prioridade, semaforo, geojson_polygon, nota_risco, kpi_retorno, gasto_publico, resultado_ambiental, pendencias_resumo, ultima_atualizacao, created_at, updated_at)
VALUES
  ('Abreulândia', '1700251', NULL, 81.24, 'VERDE', NULL, NULL, NULL, NULL, 559913.8598, NULL, NOW(), NOW(), NOW()),
  ('Aguiarnópolis', '1700301', NULL, 94.31, 'VERDE', NULL, NULL, NULL, NULL, 192057.6716, NULL, NOW(), NOW(), NOW())
ON CONFLICT (codigo_ibge) DO NOTHING;

-- Emissoes (exemplo reduzido) - insere apenas se município existir
INSERT INTO emissoes_carbono (municipio_id, data_referencia, emissao_tco2e, fonte, observacao, created_at)
SELECT m.id, DATE '2024-12-31', e.emissao_tco2e, 'SEEG v13.0', 'Fonte oficial SEEG - CO2e municipal 2024', NOW()
FROM (VALUES
  ('1700251', 559913.8598),
  ('1700301', 192057.6716)
) AS e(codigo_ibge, emissao_tco2e)
JOIN municipios m ON m.codigo_ibge = e.codigo_ibge;

-- Indicadores (exemplo reduzido)
INSERT INTO indicadores (municipio_id, ano, gasto_publico, resultado_ambiental, desmatamento_recente_factor, eficiencia_gasto, irregularidades_car, area_elegivel_ha, created_at, updated_at)
SELECT m.id, 2024, NULL, e.emissao_tco2e, m.score_prioridade, NULL, NULL, NULL, NOW(), NOW()
FROM emissoes_carbono e
JOIN municipios m ON m.id = e.municipio_id
WHERE e.data_referencia = DATE '2024-12-31';
