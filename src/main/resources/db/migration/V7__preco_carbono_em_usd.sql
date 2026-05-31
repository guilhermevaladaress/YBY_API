-- V7 - Converte os preços de demonstração em creditos_carbono de BRL para USD.
-- O campo preco_tonelada agora representa o preço em dólar americano (USD),
-- padrão do mercado voluntário global de carbono.
-- A conversão para R$ é feita dinamicamente em tempo de execução via API de cotação.
--
-- Valores originais (BRL) → Valores convertidos (USD) usando cotação ~R$5,70/USD,
-- alinhados com os preços de referência das instituições cadastradas (USD 8–15/tCO2e).

UPDATE creditos_carbono
SET preco_tonelada = CASE
    WHEN municipio_id = (SELECT id FROM municipios WHERE codigo_ibge = '1702109')
        THEN 10.20  -- Araguaina: R$58 / 5.70 ≈ USD 10.20
    WHEN municipio_id = (SELECT id FROM municipios WHERE codigo_ibge = '1717503')
        THEN 9.65   -- Pium: R$55 / 5.70 ≈ USD 9.65
    WHEN municipio_id = (SELECT id FROM municipios WHERE codigo_ibge = '1711902')
        THEN 9.12   -- Lagoa da Confusao: R$52 / 5.70 ≈ USD 9.12
    WHEN municipio_id = (SELECT id FROM municipios WHERE codigo_ibge = '1708205')
        THEN 8.77   -- Formoso do Araguaia: R$50 / 5.70 ≈ USD 8.77
    WHEN municipio_id = (SELECT id FROM municipios WHERE codigo_ibge = '1709500')
        THEN 8.42   -- Gurupi: R$48 / 5.70 ≈ USD 8.42
    WHEN municipio_id = (SELECT id FROM municipios WHERE codigo_ibge = '1721000')
        THEN 8.77   -- Palmas: R$50 / 5.70 ≈ USD 8.77
    WHEN municipio_id = (SELECT id FROM municipios WHERE codigo_ibge = '1707009')
        THEN 7.89   -- Dianopolis: R$45 / 5.70 ≈ USD 7.89
    ELSE preco_tonelada
END;

