-- V6 - Seed de demonstracao para credito rural de carbono e projetos JREDD+.
-- As tabelas creditos_carbono e projetos_jredd/marcos_projeto eram criadas vazias
-- (V4 sem seed), deixando o Painel de Carbono, a tela de Creditos, a de Projetos e o
-- matching de instituicoes sem dados. Este seed popula um conjunto inicial realista,
-- referenciando municipios reais do Tocantins pelo codigo IBGE (robusto a ids).
-- Valores sao referencias de demonstracao e podem ser editados pelo gestor.

-- ---------------------------------------------------------------------------
-- Creditos rurais de carbono (meta de tCO2e/ano por municipio).
-- cumprimento e taxa de crescimento como fracao (0-1). Biomas de maior potencial
-- no TO: faixa amazonica (Araguaina) e Cerrado/Cantao (Pium, Lagoa da Confusao).
-- ---------------------------------------------------------------------------
INSERT INTO creditos_carbono
    (municipio_id, ano_base, meta_tco2e_ano, preco_tonelada, taxa_crescimento_preco,
     percentual_cumprimento_meta, horizonte_anos, descricao, status)
VALUES
    ((SELECT id FROM municipios WHERE codigo_ibge = '1702109'), 2025, 60000.0000, 58.0000, 0.0600, 0.9000, 12,
     'Reducao de desmatamento na faixa amazonica do norte do estado (Araguaina), com monitoramento DETER/PRODES.', 'EM_ANDAMENTO'),
    ((SELECT id FROM municipios WHERE codigo_ibge = '1717503'), 2025, 45000.0000, 55.0000, 0.0600, 0.8500, 10,
     'Conservacao do Cerrado e da regiao do Cantao (Pium), com forte potencial de carbono evitado.', 'EM_ANDAMENTO'),
    ((SELECT id FROM municipios WHERE codigo_ibge = '1711902'), 2025, 38000.0000, 52.0000, 0.0500, 0.8000, 10,
     'Manejo sustentavel em Lagoa da Confusao, integrando producao de graos e areas de preservacao.', 'EM_ANDAMENTO'),
    ((SELECT id FROM municipios WHERE codigo_ibge = '1708205'), 2025, 30000.0000, 50.0000, 0.0500, 0.7500, 8,
     'Recuperacao de pastagens degradadas no Vale do Araguaia (Formoso do Araguaia).', 'PLANEJADO'),
    ((SELECT id FROM municipios WHERE codigo_ibge = '1709500'), 2025, 28000.0000, 48.0000, 0.0500, 0.8200, 10,
     'Projeto de baixo carbono na regiao sul (Gurupi), com sistemas agroflorestais.', 'EM_ANDAMENTO'),
    ((SELECT id FROM municipios WHERE codigo_ibge = '1721000'), 2025, 25000.0000, 50.0000, 0.0400, 0.7000, 10,
     'Programa de arborizacao urbana e recuperacao de APP no entorno de Palmas.', 'PLANEJADO'),
    ((SELECT id FROM municipios WHERE codigo_ibge = '1707009'), 2025, 20000.0000, 45.0000, 0.0400, 0.6000, 8,
     'Conservacao da Serra Geral em Dianopolis, com foco em nascentes e Cerrado.', 'PLANEJADO');

-- ---------------------------------------------------------------------------
-- Projetos JREDD+ (projeto guarda-chuva estadual + projetos municipais).
-- ---------------------------------------------------------------------------
INSERT INTO projetos_jredd
    (nome, descricao, municipio_id, status, data_inicio, data_fim_prevista, meta_tco2e, orcamento_previsto)
VALUES
    ('JREDD+ Jurisdicional Tocantins',
     'Programa jurisdicional de reducao de emissoes por desmatamento e degradacao no ambito do estado, alinhado ao padrao ART/TREES e a Coalizao LEAF.',
     NULL, 'EM_EXECUCAO', DATE '2024-01-15', DATE '2030-12-31', 2000000.0000, 50000000.00),
    ('Recuperacao de Pastagens - Vale do Araguaia',
     'Conversao de pastagens degradadas em sistemas ILPF e recomposicao de Reserva Legal em Formoso do Araguaia.',
     (SELECT id FROM municipios WHERE codigo_ibge = '1708205'), 'EM_EXECUCAO', DATE '2025-03-01', DATE '2029-02-28', 120000.0000, 8000000.00),
    ('Corredor Ecologico do Cantao',
     'Conservacao e restauracao no entorno do Parque Estadual do Cantao (Pium), conectando fragmentos de Cerrado e Amazonia.',
     (SELECT id FROM municipios WHERE codigo_ibge = '1717503'), 'PLANEJADO', DATE '2026-01-01', DATE '2032-12-31', 90000.0000, 6000000.00),
    ('Restauracao de APP - regiao de Araguaina',
     'Recomposicao de Areas de Preservacao Permanente em microbacias prioritarias da faixa amazonica.',
     (SELECT id FROM municipios WHERE codigo_ibge = '1702109'), 'EM_EXECUCAO', DATE '2024-08-01', DATE '2028-07-31', 75000.0000, 4500000.00);

-- ---------------------------------------------------------------------------
-- Marcos (etapas) de execucao dos projetos, referenciados pelo nome.
-- ---------------------------------------------------------------------------
INSERT INTO marcos_projeto (projeto_id, titulo, descricao, data_prevista, data_conclusao, status, percentual_conclusao)
VALUES
    ((SELECT id FROM projetos_jredd WHERE nome = 'JREDD+ Jurisdicional Tocantins'),
     'Aprovacao da metodologia ART/TREES', 'Submissao e aprovacao do documento de concepcao do programa.',
     DATE '2024-06-30', DATE '2024-06-20', 'CONCLUIDO', 100),
    ((SELECT id FROM projetos_jredd WHERE nome = 'JREDD+ Jurisdicional Tocantins'),
     'Validacao da linha de base de desmatamento', 'Definicao da linha de base com series PRODES/DETER.',
     DATE '2024-12-31', DATE '2024-12-10', 'CONCLUIDO', 100),
    ((SELECT id FROM projetos_jredd WHERE nome = 'JREDD+ Jurisdicional Tocantins'),
     'Primeira emissao de creditos jurisdicionais', 'Verificacao independente e emissao do primeiro lote de creditos.',
     DATE '2026-06-30', NULL, 'EM_ANDAMENTO', 40),
    ((SELECT id FROM projetos_jredd WHERE nome = 'JREDD+ Jurisdicional Tocantins'),
     'Auditoria independente do periodo 1', 'Auditoria de terceira parte do primeiro periodo de monitoramento.',
     DATE '2027-03-31', NULL, 'PENDENTE', 0),
    ((SELECT id FROM projetos_jredd WHERE nome = 'Recuperacao de Pastagens - Vale do Araguaia'),
     'Diagnostico de areas degradadas', 'Mapeamento e priorizacao das areas de pastagem degradada.',
     DATE '2025-06-30', DATE '2025-06-25', 'CONCLUIDO', 100),
    ((SELECT id FROM projetos_jredd WHERE nome = 'Recuperacao de Pastagens - Vale do Araguaia'),
     'Implantacao ILPF - fase 1', 'Primeira fase de integracao lavoura-pecuaria-floresta.',
     DATE '2026-12-31', NULL, 'EM_ANDAMENTO', 55),
    ((SELECT id FROM projetos_jredd WHERE nome = 'Restauracao de APP - regiao de Araguaina'),
     'Cercamento e protecao de nascentes', 'Isolamento das areas de nascente para regeneracao natural.',
     DATE '2025-03-31', DATE '2025-04-20', 'ATRASADO', 70);
