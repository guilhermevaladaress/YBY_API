<div align="center">

# 🌱 YBY API — GESTÃO AMBIENTAL

### API de Inteligência para Gestão Ambiental Acessível, Eficiente e Orientada por Dados

**1ª EDIÇÃO FAPTGULHAS – HACKAMARH – EDIÇÃO HACKATHON (2026)**
**Eixo 1 — Acesso ao Financiamento e Gestão de Impacto (JREDD+)**

`Equipe YBY`

</div>

## 📋 Índice

1. [Sobre o projeto](#-sobre-o-projeto)
2. [Stack e tecnologias (versões)](#-stack-e-tecnologias-versões)
3. [Pré-requisitos](#-pré-requisitos)
4. [Como rodar a aplicação (passo a passo)](#️-como-rodar-a-aplicação-passo-a-passo)
5. [Acessando a API (Swagger + login)](#-acessando-a-api-swagger--login)
6. [Banco de dados](#-banco-de-dados)
7. [Configuração e variáveis de ambiente](#-configuração-e-variáveis-de-ambiente)
8. [Arquitetura](#-arquitetura)
9. [Principais endpoints](#-principais-endpoints)
10. [Testes](#-testes)
11. [Uso de IA no desenvolvimento](#-uso-de-ia-no-desenvolvimento)
12. [Equipe](#-equipe)
13. [Licença](#-licença)

---

## 🎯 Sobre o projeto

O **YBY API** é o back-end do **JREDD+ Intelligence**, uma plataforma para apoiar a **decisão de investimento ambiental no Estado do Tocantins**. A API expõe serviços REST que ajudam gestores públicos a:

- **Priorizar municípios** para investimento ambiental (score de prioridade 0–100);
- **Democratizar o acesso a fundos de carbono e impacto (JREDD+)**, com fluxos de criação de projetos, instituições financiadoras e match de créditos;
- **Medir o retorno por real investido** (KPI de retorno ambiental/gasto público);
- **Identificar desperdício de gasto** e gargalos via benchmarking estadual;
- **Analisar risco e prontidão** para investir (semáforo bioma-sensível, risco preditivo);
- **Projetar emissões e desmatamento** com séries históricas (SEEG) e focos de calor (INPE);
- **Garantir transparência e rastreabilidade** com endpoints públicos e auditoria de escrita.

> Alinhado ao **Art. 4º** e ao **Eixo 1** do edital HACKAMARH: democratização do acesso a fundos de carbono (JREDD+), com transparência e rastreabilidade dos ativos naturais do Tocantins.

---

## 🧰 Stack e tecnologias (versões)

| Tecnologia | Versão | Observação |
|---|---|---|
| **Java (JDK)** | **21** (LTS) | `java.version=21` no `pom.xml`. Testado com Temurin/Microsoft OpenJDK 21. |
| **Spring Boot** | **4.0.6** | Parent `spring-boot-starter-parent`. |
| **Maven** | **3.9.16** | Não precisa instalar — usar o **Maven Wrapper** (`./mvnw`). |
| **Spring Web (MVC)** | gerenciado pelo Boot | API REST. |
| **Spring Data JPA / Hibernate** | gerenciado pelo Boot | Persistência. `ddl-auto=validate` (schema é responsabilidade do Flyway). |
| **Spring Security + JWT** | gerenciado pelo Boot | Autenticação stateless. |
| **JJWT (io.jsonwebtoken)** | **0.12.6** | Geração/validação de tokens JWT. |
| **Bean Validation** | gerenciado pelo Boot | `spring-boot-starter-validation`. |
| **Flyway** | gerenciado pelo Boot | Migrations versionadas (`V1`…`V9`). `flyway-database-postgresql` incluído. |
| **PostgreSQL Driver** | gerenciado pelo Boot | Para ambiente de produção (opcional). |
| **H2 Database** | gerenciado pelo Boot | Banco **em memória** padrão (modo `PostgreSQL`) — usado para rodar sem instalar nada. |
| **springdoc-openapi (Swagger UI)** | **2.8.8** | Documentação interativa em `/swagger-ui.html`. |
| **OpenPDF (librepdf)** | **2.0.5** | Geração de relatórios PDF. |
| **Lombok** | gerenciado pelo Boot | Redução de boilerplate. |
| **JUnit 5 / Spring Boot Test / Spring Security Test** | gerenciado pelo Boot | Testes de unidade e integração. |


---

## ✅ Pré-requisitos

- **JDK 21** instalado e disponível no `PATH` (verifique com `java -version`).

Tudo o mais (Maven, banco de dados, dependências) é resolvido automaticamente:

- **Maven** → use o wrapper incluído (`mvnw` / `mvnw.cmd`);
- **Banco de dados** → o padrão é **H2 em memória**;
- **Dependências** → baixadas automaticamente pelo Maven no primeiro build (requer **conexão com a internet** no primeiro `build/run`).

### Verificando o JDK 21

```bash
java -version
```

Saída esperada (a versão deve começar com `21`):

```
openjdk version "21.0.x" ...
```

> Se você não tiver o JDK 21, baixe em: [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21) ou [Microsoft OpenJDK 21](https://learn.microsoft.com/java/openjdk/download).

---

## ▶️ Como rodar a aplicação (passo a passo)

### 1. Clonar o repositório

```bash
git clone https://github.com/guilhermevaladaress/YBY_API.git
cd YBY_API
```

### 2. Subir a aplicação (modo padrão — H2 em memória)

Escolha o comando conforme o seu sistema operacional.

**Windows (PowerShell ou CMD):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

> Na primeira execução, o Maven Wrapper baixa o Maven 3.9.16 e todas as dependências. Isso pode levar alguns minutos. Execuções seguintes são rápidas.

### 3. Pronto! ✅

Quando aparecer no console algo como:

```
Started YbyApiApplication in X.XXX seconds
```

a API estará rodando em:

- **Base da API:** http://localhost:8080
- **Swagger UI (documentação interativa):** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

O que acontece automaticamente ao subir:

- O **Flyway** cria todo o schema e popula o banco com **dados de exemplo** (municípios, indicadores, créditos de carbono, projetos JREDD+, séries históricas de emissão etc.);
- Um **usuário GESTOR de teste** já é criado (veja credenciais abaixo);
- A **inteligência preditiva** é recalculada na subida (focos INPE + série histórica).

---

### (Alternativa) Gerar e rodar o JAR

```bash
# Windows
.\mvnw.cmd clean package
java -jar target\yby-api-0.0.1-SNAPSHOT.jar

# Linux / macOS
./mvnw clean package
java -jar target/yby-api-0.0.1-SNAPSHOT.jar
```

> Para pular os testes durante o empacotamento: adicione `-DskipTests`.

---

## 🔐 Acessando a API (Swagger + login)

A maioria dos endpoints exige autenticação **JWT (Bearer Token)**. O fluxo é:

### 1. Fazer login (endpoint público)

`POST /api/v1/auth/login`

Use o usuário **GESTOR** já incluído nos dados de exemplo (criado via migration Flyway `V3`):

```json
{
  "email": "admin.teste@yby.local",
  "senha": "Gestor@123"
}
```

A resposta traz o token:

```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1Ni␣...",
  "expiresIn": 28800,
  "role": "GESTOR"
}
```

### 2. Usar o token no Swagger

1. Abra http://localhost:8080/swagger-ui.html
2. Clique em **Authorize** (cadeado no topo).
3. Cole o token (o formato esperado é `Bearer <token>` — confira a indicação no próprio botão).
4. Agora você pode testar todos os endpoints autenticados direto pelo Swagger.

### 3. Usar o token via cURL

```bash
# 1) Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin.teste@yby.local","senha":"Gestor@123"}'

# 2) Chamada autenticada (substitua <TOKEN>)
curl http://localhost:8080/api/v1/municipios/ranking \
  -H "Authorization: Bearer <TOKEN>"
```

### Credenciais de teste

| Perfil | E-mail | Senha | Como é criado |
|---|---|---|---|
| **GESTOR** | `admin.teste@yby.local` | `Gestor@123` | Migration Flyway `V3` (sempre disponível) |

> Endpoints **públicos** (sem token): `POST /api/v1/auth/login`, `POST /api/v1/auth/esqueci-senha`, `POST /api/v1/auth/redefinir-senha`, `GET /api/v1/public/**`, e o Swagger/OpenAPI.

---

## 🗄️ Banco de dados

O projeto suporta **dois modos**. Para a avaliação, recomendamos o **modo padrão (H2)** — zero configuração.

### Modo padrão — H2 em memória (recomendado)

- Não exige instalação de nada.
- O banco é recriado a cada subida e populado pelo Flyway (schema + dados de exemplo).
- H2 roda em **modo de compatibilidade PostgreSQL**, então o comportamento é equivalente ao banco de produção.
- **Console H2** (opcional, para inspeção): habilitável; por padrão o foco é o Swagger.

### Modo produção — PostgreSQL (opcional)

Basta definir as variáveis de ambiente do datasource antes de subir. Exemplo:

```bash
# Linux / macOS
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/yby_api"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/yby_api"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
.\mvnw.cmd spring-boot:run
```

> O Flyway aplicará as mesmas migrations no PostgreSQL. O schema é **fonte de verdade do Flyway**; o Hibernate apenas **valida** o mapeamento (`ddl-auto=validate`).

### Migrations (Flyway)

Localizadas em [`src/main/resources/db/migration`](src/main/resources/db/migration):

| Migration | Conteúdo |
|---|---|
| `V1__init_schema.sql` | Schema inicial (usuários, municípios, indicadores, desmatamento, alertas, emissões, jobs, audit_log) |
| `V2__seed.sql` | Dados iniciais (municípios e indicadores) |
| `V3__seed_admin_teste.sql` | Usuário GESTOR de teste |
| `V4__creditos_carbono_e_projetos.sql` | Créditos de carbono e projetos JREDD+ |
| `V5__plano_safra_e_instituicoes_carbono.sql` | Linhas de crédito (plano safra) e instituições de carbono |
| `V6__seed_creditos_carbono_e_projetos.sql` | Seed de créditos e projetos |
| `V7__preco_carbono_em_usd.sql` | Preço de carbono em USD |
| `V8__emissoes_carbono_serie_historica.sql` | Série histórica de emissões (SEEG) |
| `V9__inteligencia_desmatamento_alertas_projecoes.sql` | Projeções de inteligência preditiva |

**Tabelas principais:** `usuarios`, `municipios`, `indicadores`, `desmatamento`, `alertas`, `emissoes_carbono`, `import_jobs`, `audit_log`, `creditos_carbono`, `projetos_jredd`, `marcos_projeto`, `linhas_credito_safra`, `instituicoes_carbono`, `projecoes_inteligencia`.

---

## ⚙️ Configuração e variáveis de ambiente

Todas as configurações têm **valores padrão** (em [`application.properties`](src/main/resources/application.properties)) — **nada é obrigatório** para rodar em modo de avaliação. As variáveis abaixo permitem customização:

| Variável | Padrão | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | H2 em memória (modo PostgreSQL) | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | *(vazio)* | Senha do banco |
| `JWT_SECRET` | chave de dev | Segredo de assinatura do JWT (**troque em produção**) |
| `FLYWAY_ENABLED` | `true` | Habilita migrations |
| `JPA_DDL_AUTO` | `validate` | Estratégia DDL do Hibernate |
| `CORS_ALLOWED_ORIGINS` | `localhost:3000, localhost:5173` | Origens permitidas (CORS) |
| `CORS_ALLOW_ALL_IN_DEV` | `true` | Libera CORS para `*` em desenvolvimento |
| `INTELIGENCIA_SCHEDULER_ENABLED` | `true` | Recalcula inteligência preditiva diariamente |
| `INTELIGENCIA_SCHEDULER_CRON` | `0 0 3 * * *` | Agendamento do recálculo |
| `RESEND_API_KEY` | *(vazio)* | Chave do Resend para e-mail de recuperação de senha (opcional). Sem ela, o token de redefinição volta na própria resposta (fallback de dev) |
| `MAIL_FROM` / `FRONTEND_URL` | valores de dev | Remetente e URL do front |

> **Tempo de expiração do JWT:** 8 horas (`28800s`). **Senhas:** armazenadas sempre com **BCrypt**.

### Integrações externas (fontes oficiais — públicas e gratuitas)

Configuráveis via prefixo `app.integracao` ([`IntegracaoProperties`](src/main/java/com/yby/api/config/IntegracaoProperties.java)):

- **IBGE** — Serviço de dados de municípios (`servicodados.ibge.gov.br`)
- **INPE / Queimadas** — Focos de calor (`queimadas.dgi.inpe.br`)
- **SEPLAN-TO Geoportal** — Camadas territoriais (GeoServer público `geoserver.seplan.to.gov.br`)

---

## 🏛️ Arquitetura

Organização em camadas (Spring Boot, pacote base `com.yby.api`):

```
src/main/java/com/yby/api
├── config/        # Configurações (Security, CORS, propriedades, seeders)
├── controller/    # Endpoints REST (sem regra de negócio pesada)
├── service/       # Regras de negócio e cálculos
│   └── inteligencia/   # Score, risco preditivo, projeções, alocação, equidade...
├── repository/    # Acesso a dados (Spring Data JPA)
├── entity/        # Entidades JPA
│   └── enums/     # Domínios (Role, Bioma, Semaforo, ...)
├── dto/           # Contratos de entrada/saída (nunca expõe entidade)
├── mapper/        # Conversões entity <-> DTO
├── security/      # JWT, filtros, UserDetails
├── integration/   # Clientes de fontes externas (IBGE, INPE, SEPLAN)
└── exception/     # Tratamento global (RFC 7807 / Problem Details)

src/main/resources
├── application.properties
└── db/migration/  # Migrations Flyway (V1...V9)
```

**Convenções principais:**

- Todas as rotas usam o prefixo `/api/v1`.
- Perfis: `GESTOR` e `SERVIDOR` (roles Spring `ROLE_GESTOR` / `ROLE_SERVIDOR`).
- Autenticação **stateless** via `Authorization: Bearer <token>`.
- Erros seguem **RFC 7807 (Problem Details)**.
- Datas em **ISO 8601**.
- Endpoints de mapa retornam `Content-Type: application/geo+json`.
- Toda escrita por `GESTOR` é registrada em `audit_log`.

> Documentação de domínio detalhada: [`docs/AGENTS.md`](docs/AGENTS.md) e [`docs/REGRAS_NEGOCIO_INOVACAO.md`](docs/REGRAS_NEGOCIO_INOVACAO.md).

---

## 🌐 Principais endpoints

Base: `http://localhost:8080/api/v1` — explore tudo via **Swagger UI**.

### Autenticação — `/auth`
| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/auth/login` | Público |
| `POST` | `/auth/esqueci-senha` | Público |
| `POST` | `/auth/redefinir-senha` | Público |
| `GET` | `/auth/me` | Autenticado |
| `PATCH` | `/auth/senha` | Autenticado |

### Municípios — `/municipios`
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/municipios/ranking` | Ranking paginado por score de prioridade |
| `GET` | `/municipios/geojson` | FeatureCollection GeoJSON |
| `GET` | `/municipios/{id}` | Detalhe do município |
| `POST` / `PUT` / `DELETE` | `/municipios` | Gestão (GESTOR) |

### Indicadores / KPI — `/indicadores`
| `GET` | `/indicadores/{municipioId}/kpi` | KPI agregado por período |
| `GET` | `/indicadores/{municipioId}/historico` | Série anual |
| `POST` | `/indicadores` | Upsert anual (GESTOR) |

### Inteligência preditiva — `/inteligencia`
- `GET /inteligencia/tendencia/{municipioId}`
- `GET /inteligencia/kpi-multidimensional/{municipioId}`
- `GET /inteligencia/semaforo-bioma/{municipioId}`
- `GET /inteligencia/risco-preditivo/{municipioId}`
- `GET /inteligencia/carbono-evitado/{municipioId}`
- `GET /inteligencia/projecao-desmatamento/{municipioId}`
- `GET /inteligencia/roi-desmatamento-evitado/{municipioId}`
- `GET /inteligencia/desperdicio` · `GET /inteligencia/equidade`
- `POST /inteligencia/alocacao` · `POST /inteligencia/recalcular`

### Crédito de Carbono / JREDD+
- `/creditos-carbono` — CRUD de créditos + projeção
- `/instituicoes-carbono` — instituições financiadoras + match (`/match/credito/{creditoId}`)
- `/projetos` — projetos JREDD+ e marcos (`/{id}/marcos`)
- `/carbono` e `/carbono/dashboard` — históricos, registros, sincronização, relatório PDF
- `/dashboard` — resumo e projeção de carbono
- `/plano-safra` — linhas de crédito e simulação
- `/geoportal` — camadas e sugestões de áreas de carbono (SEPLAN-TO)

### Público (transparência, sem login) — `/public`
| `GET` | `/public/ranking` | Ranking aberto |
| `GET` | `/public/geojson` | GeoJSON aberto |

### Administração (GESTOR) — `/admin`
- `/admin/usuarios` (CRUD + `/{id}/status`)
- `/admin/relatorio` · `/admin/sync/ibge/municipios`

---

## 🧪 Testes

O projeto inclui testes de **unidade** (serviços de inteligência) e de **integração** (controllers, autenticação e autorização).

```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

Os testes de integração sobem o contexto Spring com banco H2, validando os fluxos de autenticação JWT, regras de acesso `GESTOR/SERVIDOR` e os cálculos de score, KPI, risco e projeções.

---

## 🤖 Uso de IA no desenvolvimento

Em conformidade com o **Art. 26, §4º** do edital (uso de ferramentas de IA como apoio, mantendo a equipe responsável pela autoria, validação e funcionamento da solução), declaramos que utilizamos as seguintes ferramentas durante o desenvolvimento do back-end:

- **OpenAI Codex**
- **Claude (Anthropic)**

A IA foi usada como **apoio** à escrita de código, geração de testes e documentação. Toda a arquitetura, regras de negócio, validação e funcionamento foram **revisados e validados pela Equipe YBY**.

### Exemplos de prompts utilizados no back-end

**Prompt 1 — Modelagem e camadas (scaffold do domínio):**
> "Crie a estrutura inicial de uma API REST em Java 21 + Spring Boot com Spring Web, Spring Data JPA, Spring Security com JWT stateless, Flyway e Swagger. Organize em camadas (controller, service, repository, entity, dto, mapper, security, config, exception). Todas as rotas devem usar o prefixo `/api/v1`, com perfis `GESTOR` e `SERVIDOR`, token JWT com expiração de 8 horas e tratamento de erros no padrão RFC 7807 (Problem Details)."

**Prompt 2 — Regra de negócio (score de prioridade):**
> "Implemente no service o cálculo do `score_prioridade` de um município, variando de 0 a 100, combinando os fatores com pesos: desmatamento recente 40%, eficiência de gasto 30%, irregularidades CAR 20% e área elegível 10%. Se algum fator estiver indisponível, reponderar proporcionalmente os fatores disponíveis sem zerar o município. Nunca dividir por zero ao calcular o KPI de retorno (resultado_ambiental / gasto_publico) e cubra a lógica com testes unitários JUnit 5."

**Prompt 3 — Integração externa e inteligência preditiva:**
> "Crie um client para consumir os focos de calor da API pública de queimadas do INPE e um scheduler (`@Scheduled`) que, na subida da aplicação e diariamente às 03h, recalcule as projeções de desmatamento e emissões de carbono usando a série histórica do SEEG, persistindo o resultado na tabela `projecoes_inteligencia`. Não rode a integração pesada automaticamente durante os testes."

---

## 👥 Equipe

**Equipe YBY**

| Integrante |
|---|
| Marcos Ribeiro |
| Guilherme Valadares |
| Pedro Lucas |
| Ana Carolina |
| Rafael Diniz |


---

## 📜 Licença

Projeto desenvolvido para a **1ª Edição FAPTGULHAS – HACKAMARH – Edição Hackathon (2026)**, promovido pela **SEMARH** e pela **UNITINS**, por meio do **NIT**, com apoio da **FAPT**.

Conforme **Art. 64 e Art. 65** do edital, a propriedade intelectual pertence aos seus autores (Equipe YBY) e recomenda-se a adoção de **licenças abertas (Open Source)**.

<div align="center">

---

**🌱 YBY** — *"yby" significa "terra" / "solo" em Tupi-Guarani.*
Tecnologia para uma gestão ambiental acessível, eficiente e orientada por dados.

</div>
