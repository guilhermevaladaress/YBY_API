CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('GESTOR', 'SERVIDOR')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    troca_senha_primeiro_acesso BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS municipios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    codigo_ibge VARCHAR(7) NOT NULL UNIQUE CHECK (codigo_ibge ~ '^[0-9]{7}$'),
    area_ha NUMERIC(14,2),
    score_prioridade NUMERIC(5,2) CHECK (score_prioridade >= 0 AND score_prioridade <= 100),
    semaforo VARCHAR(12) NOT NULL CHECK (semaforo IN ('VERDE', 'AMARELO', 'VERMELHO')),
    geojson_polygon JSONB,
    ultima_atualizacao TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS indicadores (
    id BIGSERIAL PRIMARY KEY,
    municipio_id BIGINT NOT NULL REFERENCES municipios(id) ON DELETE CASCADE,
    ano INTEGER NOT NULL,
    gasto_publico NUMERIC(14,2),
    resultado_ambiental NUMERIC(14,2),
    eficiencia_gasto NUMERIC(6,2),
    irregularidades_car INTEGER,
    area_elegivel_ha NUMERIC(14,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_indicadores_municipio_ano UNIQUE (municipio_id, ano)
);

CREATE TABLE IF NOT EXISTS desmatamento (
    id BIGSERIAL PRIMARY KEY,
    municipio_id BIGINT NOT NULL REFERENCES municipios(id) ON DELETE CASCADE,
    fonte VARCHAR(10) NOT NULL CHECK (fonte IN ('DETER', 'PRODES')),
    data_referencia DATE NOT NULL,
    area_desmatada_ha NUMERIC(12,2) NOT NULL,
    bioma VARCHAR(60)
);

CREATE TABLE IF NOT EXISTS alertas (
    id BIGSERIAL PRIMARY KEY,
    municipio_id BIGINT NOT NULL REFERENCES municipios(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('EMBARGO', 'SOBREPOSICAO', 'CAR_IRREGULAR', 'MANUAL')),
    gravidade VARCHAR(10) NOT NULL CHECK (gravidade IN ('BAIXA', 'MEDIA', 'ALTA')),
    descricao VARCHAR(500) NOT NULL,
    resolvido BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    usuario_email VARCHAR(180) NOT NULL,
    acao VARCHAR(60) NOT NULL,
    entidade VARCHAR(80) NOT NULL,
    entidade_id VARCHAR(80),
    payload_relevante TEXT,
    "timestamp" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sync_jobs (
    id UUID PRIMARY KEY,
    fonte VARCHAR(20) NOT NULL CHECK (fonte IN ('DETER', 'PRODES', 'IBGE')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSANDO', 'CONCLUIDO', 'ERRO')),
    registros_inseridos INTEGER NOT NULL DEFAULT 0,
    mensagem_erro TEXT,
    iniciado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finalizado_em TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS relatorios (
    id BIGSERIAL PRIMARY KEY,
    municipio_id BIGINT NOT NULL REFERENCES municipios(id) ON DELETE CASCADE,
    titulo VARCHAR(180) NOT NULL,
    resumo TEXT NOT NULL,
    recomendacoes TEXT,
    data_referencia DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
