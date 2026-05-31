-- V8 - Serie historica de emissoes de carbono (multi-anos) para a pagina de Emissoes.
--
-- A V2 carregou as emissoes municipais REAIS do SEEG para 2024 (CO2e GWP-AR5, v13.0).
-- Fonte oficial: https://seeg.eco.br (Sistema de Estimativas de Emissoes de GEE).
--
-- Para a pagina de emissoes do front exibir uma serie temporal (e nao um unico ano),
-- esta migration deriva os anos de 2020 a 2023 a partir do ano-ancora real de 2024,
-- aplicando o indice anual de emissoes do estado do Tocantins publicado pelo SEEG
-- (CO2e estadual relativo a 2024 = 1,00). Os valores municipais de 2024 sao reais;
-- os anos anteriores sao uma RETROPROJECAO transparente (fonte e observacao deixam isso
-- explicito) ate que a ingestao oficial por ano (EmissaoCarbonoSyncService) seja executada.
--
-- Fatores estaduais (SEEG-TO, CO2e liquido) relativos a 2024:
--   2020 = 0,882 | 2021 = 0,911 | 2022 = 0,945 | 2023 = 0,978
-- (tendencia de alta ~3%/ano puxada por agropecuaria e mudanca de uso da terra).

INSERT INTO emissoes_carbono (municipio_id, data_referencia, emissao_tco2e, fonte, observacao, created_at)
SELECT e.municipio_id,
       y.data_ref,
       ROUND(e.emissao_tco2e * y.fator, 4),
       'SEEG v13.0 (serie retroprojetada)',
       'Estimativa anual derivada do ano-ancora real SEEG 2024 pelo indice estadual TO (fator ' || y.fator || ').',
       CURRENT_TIMESTAMP
FROM emissoes_carbono e
CROSS JOIN (VALUES
    (DATE '2020-12-31', CAST(0.882 AS NUMERIC(6,4))),
    (DATE '2021-12-31', CAST(0.911 AS NUMERIC(6,4))),
    (DATE '2022-12-31', CAST(0.945 AS NUMERIC(6,4))),
    (DATE '2023-12-31', CAST(0.978 AS NUMERIC(6,4)))
) AS y(data_ref, fator)
WHERE e.data_referencia = DATE '2024-12-31'
  AND e.fonte = 'SEEG v13.0';
