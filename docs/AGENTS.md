# AGENTS.md - JREDD+ Intelligence Back-End

## 1) Objetivo

Este repositorio contem o back-end do JREDD+ Intelligence.
O objetivo e expor API REST para decisao de investimento ambiental no Tocantins, com foco em:

1. priorizacao de municipios para investimento;
2. medicao de retorno por real investido;
3. identificacao de desperdicio de gasto;
4. analise de risco e prontidao para investir;
5. prestacao de contas auditavel.

---

## 2) Fonte de verdade do dominio

Este arquivo e a referencia operacional para desenvolvimento.
Quando houver conflito entre implementacao e regras abaixo, seguir este documento.
Nao inferir regra de negocio fora do que esta definido aqui sem registrar pendencia.

Documento complementar (extensoes inovadoras):
- `docs/REGRAS_NEGOCIO_INOVACAO.md`

---

## 3) Stack obrigatoria

- Java + Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL (preferencialmente Supabase free)
- Flyway
- Springdoc / Swagger UI
- Bean Validation

Nao trocar stack sem justificativa tecnica clara.

---

## 4) Arquitetura obrigatoria

Organizar em camadas:

- `controller` (ou `resource`): HTTP, sem regra de negocio pesada
- `service`: regras de negocio e calculos
- `repository`: acesso a dados
- `entity` (ou `model`): entidades JPA
- `dto`: contratos de entrada/saida
- `mapper`: conversoes
- `security`: JWT/autenticacao/autorizacao
- `config`: configuracoes
- `exception`: tratamento global
- `integration`: fontes externas
- `job` (ou `scheduler`): cargas periodicas

Migrations Flyway devem ficar em `src/main/resources/db/migration`.

---

## 5) Regras de negocio explicitas (obrigatorias)

### 5.1 Regras transversais

- `RN-001`: Toda rota deve usar prefixo `/api/v1`.
- `RN-002`: Perfis validos: `GESTOR` e `SERVIDOR`.
- `RN-003`: Roles no Spring Security: `ROLE_GESTOR` e `ROLE_SERVIDOR`.
- `RN-004`: JWT stateless no header `Authorization: Bearer <token>`.
- `RN-005`: Tempo de expiracao do token: `8 horas` (`expiresIn = 28800`).
- `RN-006`: Endpoint publico inicial: apenas `POST /api/v1/auth/login`.
- `RN-007`: Toda operacao de escrita por `GESTOR` (POST/PUT/PATCH/DELETE) deve gerar auditoria em `audit_log`.
- `RN-008`: Erros devem seguir RFC 7807 (Problem Details) sempre que possivel.
- `RN-009`: Datas devem usar ISO 8601 (`yyyy-MM-dd` e timestamp ISO completo).
- `RN-010`: Nao expor entidade JPA diretamente em controller. Sempre usar DTO.
- `RN-011`: Nunca retornar `senha_hash` em respostas.
- `RN-012`: Senha sempre com BCrypt, nunca texto puro.
- `RN-013`: CORS restritivo em producao; `*` apenas em desenvolvimento.
- `RN-014`: Endpoints de mapa devem retornar `Content-Type: application/geo+json`.
- `RN-015`: Paginacao padrao Spring (`page` 0-based, `size`, `totalElements`, `totalPages`, `content`).

### 5.2 Regras de calculo e classificacao

- `RN-100`: `score_prioridade` deve variar de `0` a `100`.
- `RN-101`: Score do municipio combina os fatores com pesos:
  - desmatamento recente: `40%`
  - eficiencia de gasto: `30%`
  - irregularidades CAR: `20%`
  - area elegivel: `10%`
- `RN-102`: Se algum fator estiver indisponivel, calcular score apenas com fatores disponiveis e reponderar proporcionalmente (sem zerar municipio por falta de uma fonte).
- `RN-103`: KPI central: `kpi_retorno = resultado_ambiental / gasto_publico`.
- `RN-104`: Proibido dividir por zero. Se `gasto_publico <= 0` ou nulo, retornar KPI seguro (valor nulo ou convencao definida pelo DTO), sem excecao 500.
- `RN-105`: Semaforo permitido: `verde`, `amarelo`, `vermelho`.
- `RN-106`: Semaforo de prontidao:
  - `verde`: baixo risco, sem bloqueio ambiental/legal relevante
  - `amarelo`: pendencias resolviveis
  - `vermelho`: alto risco (ex.: embargo ativo, sobreposicao critica, historico grave de desperdicio)
- `RN-107`: Alerta de desperdicio deve marcar municipio com:
  - `gasto_publico` acima da mediana estadual, e
  - `resultado_ambiental` abaixo da mediana estadual
- `RN-108`: Nota de risco deve variar de `0` a `10` e considerar, no minimo:
  - embargos IBAMA
  - sobreposicoes com TI/FUNAI
  - sobreposicoes com UC/ICMBio
  - irregularidades/cancelamentos no CAR (SICAR)
- `RN-109`: Analise de risco deve informar pendencias de forma explicita e acionavel.

### 5.3 Regras de dados (modelo minimo)

Tabelas principais:

- `municipios`
- `indicadores`
- `desmatamento`
- `alertas`
- `usuarios`
- `audit_log`

Regras de coluna e dominio:

- `municipios.codigo_ibge`: obrigatorio, 7 caracteres.
- `municipios.score_prioridade`: numerico entre 0 e 100.
- `municipios.semaforo`: `verde|amarelo|vermelho`.
- `municipios.geojson_polygon`: JSONB com poligono GeoJSON valido.
- `indicadores`: chave logica por (`municipio_id`, `ano`) para evitar duplicidade anual.
- `desmatamento.fonte`: `DETER|PRODES`.
- `alertas.tipo`: `EMBARGO|SOBREPOSICAO|CAR_IRREGULAR|MANUAL` (ou equivalente mapeado no enum).
- `alertas.gravidade`: `BAIXA|MEDIA|ALTA`.
- `usuarios.email`: unico.
- `usuarios.role`: `GESTOR|SERVIDOR`.
- `usuarios.ativo`: booleano.

### 5.4 Regras de integracao externa

Integracoes devem ficar em `service` dedicado, nunca em controller.
Fontes usadas pelo dominio:

- INPE TerraBrasilis (PRODES/DETER)
- MapBiomas
- SICAR
- CIGMA/SEMARH-TO
- IBGE Geociencias
- SEEG
- Geoportal SEPLAN-TO
- Base dos Dados (uso pontual)
- IBAMA (embargos)
- FUNAI (Terras Indigenas)
- ICMBio (Unidades de Conservacao)
- Portal da Transparencia (gasto publico)

Regras operacionais:

- PRODES: carga anual.
- DETER: carga diaria via `@Scheduled` (quando habilitado no ambiente).
- Em hackathon, permitido mock/carga CSV-JSON manual, mantendo contrato de API igual ao real.
- Nao rodar integracao pesada automaticamente em testes.

### 5.5 Regras de auditoria

Toda escrita de `GESTOR` deve registrar:

- usuario executor
- acao
- entidade afetada
- timestamp
- payload relevante

Se auditoria completa nao estiver finalizada, manter estrutura pronta e contrato de log previsto.

---

## 6) Contratos de endpoints e regras especificas

## 6.1 Municipios

- `GET /api/v1/municipios/ranking`
  - roles: `GESTOR` e `SERVIDOR`
  - query: `page` (default 0), `size` (default 50), `ordenar` (`score|nome|area`), `ordem` (`desc|asc`)
  - retorno: lista paginada com `id,nome,codigoIbge,scorePrioridade,semaforo,areaHa,kpiRetorno`

- `GET /api/v1/municipios/geojson`
  - roles: `GESTOR` e `SERVIDOR`
  - query opcional: `semaforo=verde|amarelo|vermelho`
  - retorno: `FeatureCollection` GeoJSON

- `GET /api/v1/municipios/{id}`
  - roles: `GESTOR` e `SERVIDOR`
  - retorno: detalhe completo com score, semaforo, notaRisco, KPI, pendencias, ultimaAtualizacao

- `PUT /api/v1/municipios/{id}`
  - role: `GESTOR`
  - uso: correcao/atualizacao manual de campos de municipio
  - obrigatorio: auditar operacao

## 6.2 Indicadores e KPI

- `GET /api/v1/indicadores/{municipioId}/kpi`
  - roles: `GESTOR` e `SERVIDOR`
  - query: `anoInicio`, `anoFim`
  - retorno: KPI agregado do periodo

- `GET /api/v1/indicadores/{municipioId}/historico`
  - roles: `GESTOR` e `SERVIDOR`
  - query default: `anoInicio = anoAtual-5`, `anoFim = anoAtual`
  - retorno: serie anual (`gastoPublico`, `resultadoAmbiental`, `kpiRetorno`)

- `POST /api/v1/indicadores`
  - role: `GESTOR`
  - regra: inserir ou atualizar indicador anual (upsert por municipio+ano)

## 6.3 Desmatamento

- `GET /api/v1/desmatamento/{municipioId}/historico`
  - roles: `GESTOR` e `SERVIDOR`
  - query: `fonte=PRODES|DETER|ALL` (default `ALL`), `dataInicio`, `dataFim`

- `GET /api/v1/desmatamento/resumo`
  - roles: `GESTOR` e `SERVIDOR`
  - query obrigatoria: `ano`
  - retorno: total anual + breakdown por bioma e semaforo

- `POST /api/v1/desmatamento/importar`
  - role: `GESTOR`
  - processamento assincrono com retorno `jobId`
  - status inicial esperado: `PROCESSANDO`

- `GET /api/v1/desmatamento/importar/{jobId}/status`
  - role: `GESTOR`
  - status permitidos: `PROCESSANDO|CONCLUIDO|ERRO`
  - retorno inclui `registrosInseridos`, `erros[]`, `finalizadoEm`

## 6.4 Alertas

- `GET /api/v1/alertas/desperdicio`
  - roles: `GESTOR` e `SERVIDOR`
  - query: `ano` (default ano atual), `limite` (default 10)
  - regra: aplicar RN-107

- `GET /api/v1/alertas/risco/{municipioId}`
  - roles: `GESTOR` e `SERVIDOR`
  - regra: aplicar RN-108 e RN-109

- `POST /api/v1/alertas`
  - role: `GESTOR`
  - uso: cadastro/atualizacao manual de alerta

## 6.5 Autenticacao

- `POST /api/v1/auth/login` (publico)
- `GET /api/v1/auth/me` (autenticado)
- `PATCH /api/v1/auth/senha` (autenticado)

Regras adicionais:

- troca de senha deve validar senha atual;
- resposta de alteracao de senha: `204 No Content`.

## 6.6 Administracao (somente Gestor)

- `GET /api/v1/admin/usuarios`
- `POST /api/v1/admin/usuarios`
- `PATCH /api/v1/admin/usuarios/{id}/status`
- `POST /api/v1/admin/relatorio`

Regras adicionais:

- usuario novo: senha inicial aleatoria;
- senha inicial deve ser trocada no primeiro acesso;
- desativacao nao exclui historico (apenas altera status `ativo`).

---

## 7) HTTP status obrigatorios

- `200 OK`: leitura/atualizacao com corpo
- `201 Created`: criacao
- `202 Accepted`: processamento assincrono
- `204 No Content`: sucesso sem corpo
- `400 Bad Request`: payload invalido/senha fraca
- `401 Unauthorized`: sem autenticacao/token invalido
- `403 Forbidden`: sem permissao
- `404 Not Found`: recurso nao encontrado
- `409 Conflict`: conflito de unicidade (ex.: email)
- `422 Unprocessable Entity`: validacao de negocio

---

## 8) DTOs minimos esperados

- `MunicipioRankingDTO`
- `MunicipioDetalheDTO`
- `KpiDTO`
- `IndicadorAnualDTO`
- `DesmatamentoDTO`
- `DesmatamentoResumoDTO`
- `AlertaDTO`
- `RiscoDTO`
- `UsuarioDTO`
- `RelatorioDTO`
- `LoginRequestDTO`
- `LoginResponseDTO`

Validacoes de entrada via Bean Validation:

- `@NotNull`
- `@NotBlank`
- `@Email`
- `@PositiveOrZero`
- `@Min`
- `@Max`

---

## 9) Flyway e banco

- criar migrations incrementais (`V1__...`, `V2__...`);
- nao alterar migration ja aplicada;
- para qualquer mudanca de schema, criar nova migration;
- scripts em `src/main/resources/db/migration`.

---

## 10) Definicao de pronto (checklist)

Antes de finalizar qualquer tarefa:

1. projeto compila;
2. endpoints seguem `/api/v1`;
3. controle de acesso `GESTOR/SERVIDOR` aplicado corretamente;
4. DTOs nao vazam dados sensiveis;
5. regras RN-100+ (score/KPI/semaforo/risco/desperdicio) respeitadas;
6. migrations Flyway corretas;
7. auditoria de escrita por GESTOR presente ou preparada;
8. testes relevantes atualizados (service de score/KPI, auth, autorizacao, validacoes).

---

## 11) Modo de trabalho neste repositorio

Ao receber tarefa:

1. ler este `AGENTS.md`;
2. mapear impacto em controller/service/repository/dto;
3. implementar em mudancas pequenas e coesas;
4. documentar suposicoes quando houver lacuna;
5. nao inventar endpoint fora deste escopo sem alinhamento;
6. nao alterar arquitetura principal sem motivo claro.
