-- V3 - Colunas de apoio as Regras de Negocio Inovadoras (docs/REGRAS_NEGOCIO_INOVACAO.md)
-- Idempotente (IF NOT EXISTS) para conviver com o ddl-auto=update usado no ambiente de hackathon.

-- RN-106-A / RN-108-A: bioma, vulnerabilidade hidrica e plano de acao ativo no municipio
ALTER TABLE municipios ADD COLUMN IF NOT EXISTS bioma VARCHAR(20) DEFAULT 'CERRADO';
ALTER TABLE municipios ADD COLUMN IF NOT EXISTS vulnerabilidade_hidrica VARCHAR(10) DEFAULT 'BAIXA';
ALTER TABLE municipios ADD COLUMN IF NOT EXISTS plano_acao_ativo BOOLEAN NOT NULL DEFAULT FALSE;

-- RN-103-B: indicadores sociais para o KPI multidimensional
ALTER TABLE indicadores ADD COLUMN IF NOT EXISTS empregos_conservacao INTEGER;
ALTER TABLE indicadores ADD COLUMN IF NOT EXISTS familias_psa INTEGER;
ALTER TABLE indicadores ADD COLUMN IF NOT EXISTS comunidades_tradicionais INTEGER;
ALTER TABLE indicadores ADD COLUMN IF NOT EXISTS investimento_social NUMERIC(18,2);
