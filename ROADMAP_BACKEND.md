# ROADMAP_BACKEND.md - Plano de Desenvolvimento

## Visao geral

Este roadmap organiza o desenvolvimento completo do back-end do JREDD+ Intelligence ate a entrega final, usando o `AGENTS.md` como fonte de verdade operacional.

Premissas obrigatorias que valem para todas as etapas:

- [ ] Prefixo de rotas: `/api/v1` (RN-001)
- [ ] Perfis e roles: `GESTOR`/`SERVIDOR` e `ROLE_GESTOR`/`ROLE_SERVIDOR` (RN-002/RN-003)
- [ ] JWT stateless no header `Authorization: Bearer <token>` com expiracao de 8h (RN-004/RN-005)
- [ ] DTO em todas as respostas (sem expor entidade JPA) e sem vazamento de `senha_hash` (RN-010/RN-011)
- [ ] Senhas com BCrypt (RN-012)
- [ ] Erros padronizados com Problem Details / RFC 7807 (RN-008)
- [ ] Datas em ISO 8601 (RN-009)
- [ ] CORS restritivo em producao (RN-013)
- [ ] Endpoints de mapa com `Content-Type: application/geo+json` (RN-014)
- [ ] Paginacao padrao Spring para listas (RN-015)
- [ ] Auditoria de escrita por `GESTOR` em `audit_log` (RN-007/RN-5.5)

## Ordem recomendada de desenvolvimento

1. Etapa 01 - Setup inicial do projeto [Dev A]
2. Etapa 02 - Configuracao de dependencias e baseline tecnico [Dev A]
3. Etapa 03 - Configuracao do banco PostgreSQL [Dev A]
4. Etapa 04 - Migrations com Flyway e schema inicial [Dev A]
5. Etapa 05 - Estrutura base de pacotes e camadas [Dev A]
6. Etapa 06 - Tratamento global de excecoes + Problem Details [Dev B]
7. Etapa 07 - Seguranca base com Spring Security e controle de acesso [Dev A]
8. Etapa 08 - Autenticacao JWT e endpoints de autenticacao [Dev A]
9. Etapa 09 - CRUD base de usuarios e endpoints de administracao [Dev B]
10. Etapa 10 - Endpoints base de municipios (ranking, geojson, detalhe) [Dev A]
11. Etapa 11 - Calculo de score de prioridade e semaforo [Dev A]
12. Etapa 12 - Endpoints de indicadores e KPI [Dev B]
13. Etapa 13 - Endpoints de desmatamento (historico e resumo) [Dev B]
14. Etapa 14 - Importacao INPE e status de job assincrono [Dev B]
15. Etapa 15 - Endpoints de alertas (desperdicio, risco, cadastro manual) [Dev A]
16. Etapa 16 - Integracoes externas e jobs agendados com `@Scheduled` [Dev B]
17. Etapa 17 - Logs de auditoria completos [Dev A]
18. Etapa 18 - Geracao de relatorio [Dev B]
19. Etapa 19 - Swagger e documentacao da API [Dev B]
20. Etapa 20 - Testes automatizados [Dev A + Dev B]
21. Etapa 21 - Ajustes finais para entrega [Dev A | apoio Dev B]

## Distribuicao para 2 desenvolvedores (execucao recomendada)

1. Fase 1 - Gate tecnico inicial
Dev A executa Etapas 01-05 em sequencia. Dev B faz revisao tecnica dos PRs e prepara backlog tecnico, sem iniciar features dependentes antes da conclusao da Etapa 05.
2. Fase 2 - Fundacao de API e seguranca
Dev B executa Etapa 06 em paralelo ao Dev A na Etapa 07. Apos Etapa 07, Dev A executa Etapa 08 e Dev B inicia/finaliza Etapa 09.
3. Fase 3 - Dominio em paralelo
Dev A executa Etapas 10 -> 11 -> 15. Dev B executa Etapas 12 -> 13 -> 14.
4. Fase 4 - Integracoes e governanca
Dev B executa Etapas 16 -> 18. Dev A executa Etapa 17.
5. Fase 5 - Qualidade e entrega
Dev B executa Etapa 19. Etapa 20 e compartilhada entre Dev A e Dev B. Etapa 21 fica com ownership do Dev A e apoio do Dev B.

---

## Etapa 01 - Setup inicial do projeto [Dev A]

### Branch
`setup/initial-project-setup`

### Objetivo
Preparar o baseline de execucao local e padroes minimos para o time iniciar desenvolvimento com consistencia.

### Funcionalidades incluidas

- [ ] Validar boot da aplicacao Spring e funcionamento do `mvnw`
- [ ] Definir convencoes iniciais de nomes, pacotes e padrao de commit/PR
- [ ] Criar guia rapido de setup local para equipe

### Camadas/arquivos envolvidos

- [ ] `pom.xml`
- [ ] `src/main/java/com/yby/api/YbyApiApplication.java`
- [ ] `src/main/resources/application.properties`
- [ ] `README.md` (se ainda nao existir)

### Criterios de conclusao

- [ ] Projeto sobe localmente sem erro
- [ ] Pipeline basico de build (`mvn test`) executa
- [ ] Time consegue clonar, subir e validar ambiente inicial

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Setup inicial do projeto), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 02 - Configuracao de dependencias e baseline tecnico [Dev A]

### Branch
`chore/dependencies-and-build-baseline`

### Objetivo
Garantir que a stack obrigatoria definida no `AGENTS.md` esteja configurada no projeto.

### Funcionalidades incluidas

- [ ] Adicionar/validar dependencias Spring Web, Data JPA, Security, Validation e PostgreSQL
- [ ] Adicionar/validar JWT, Flyway e Springdoc/Swagger
- [ ] Organizar versoes e escopo de dependencias de teste

### Camadas/arquivos envolvidos

- [ ] `pom.xml`
- [ ] `src/test/java/com/yby/api/`

### Criterios de conclusao

- [ ] Dependencias obrigatorias presentes e sem conflito de versao
- [ ] Build e testes baseline executando
- [ ] Projeto pronto para inicio das features sem retrabalho de stack

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Configuracao de dependencias e baseline tecnico), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 03 - Configuracao do banco PostgreSQL [Dev A]

### Branch
`setup/postgresql-database-configuration`

### Objetivo
Configurar conexao segura e previsivel com PostgreSQL para desenvolvimento, teste e producao.

### Funcionalidades incluidas

- [ ] Configurar datasource PostgreSQL por profile (`local`, `test`, `prod`)
- [ ] Definir variaveis de ambiente para credenciais e conexao
- [ ] Padronizar timezone e parametros de pool de conexao

### Camadas/arquivos envolvidos

- [ ] `src/main/resources/application.properties`
- [ ] `src/main/resources/application-local.properties` (ou equivalente)
- [ ] `src/main/resources/application-test.properties` (ou equivalente)
- [ ] `src/main/resources/application-prod.properties` (ou equivalente)
- [ ] `src/main/java/com/yby/api/config/`

### Criterios de conclusao

- [ ] Aplicacao conecta no PostgreSQL sem credenciais hardcoded
- [ ] Profiles testados com troca de ambiente
- [ ] Configuracao pronta para Flyway

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Configuracao do banco PostgreSQL), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 04 - Migrations com Flyway e schema inicial [Dev A]

### Branch
`setup/flyway-initial-migrations`

### Objetivo
Criar base de dados versionada com tabelas minimas obrigatorias do dominio e constraints principais.

### Funcionalidades incluidas

- [ ] Criar migrations iniciais (`V1__...`, `V2__...`) sem alterar scripts ja aplicados
- [ ] Criar tabelas: `municipios`, `indicadores`, `desmatamento`, `alertas`, `usuarios`, `audit_log`
- [ ] Aplicar constraints de dominio (unicidade, enums, faixas numericas, chaves logicas)

### Camadas/arquivos envolvidos

- [ ] `src/main/resources/db/migration/`
- [ ] `src/main/java/com/yby/api/entity/`
- [ ] `src/main/java/com/yby/api/repository/`

### Criterios de conclusao

- [ ] Banco cria schema completo via Flyway em ambiente limpo
- [ ] Regras minimas de colunas do AGENTS.md refletidas no schema
- [ ] Versionamento de migration pronto para evolucao incremental

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Migrations com Flyway e schema inicial), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 05 - Estrutura base de pacotes e camadas [Dev A]

### Branch
`chore/base-package-layer-structure`

### Objetivo
Aplicar arquitetura obrigatoria em camadas para acelerar implementacao das features de dominio.

### Funcionalidades incluidas

- [ ] Criar pacotes: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `security`, `config`, `exception`, `integration`, `job`
- [ ] Definir convencoes para DTOs, mappers e contratos de resposta paginada
- [ ] Preparar classes base/utilitarias para reduzir codigo repetido

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/`
- [ ] `src/main/java/com/yby/api/service/`
- [ ] `src/main/java/com/yby/api/repository/`
- [ ] `src/main/java/com/yby/api/entity/`
- [ ] `src/main/java/com/yby/api/dto/`
- [ ] `src/main/java/com/yby/api/mapper/`
- [ ] `src/main/java/com/yby/api/security/`
- [ ] `src/main/java/com/yby/api/config/`
- [ ] `src/main/java/com/yby/api/exception/`
- [ ] `src/main/java/com/yby/api/integration/`
- [ ] `src/main/java/com/yby/api/job/`

### Criterios de conclusao

- [ ] Estrutura de pacotes padronizada e versionada
- [ ] Time consegue desenvolver novas features sem divergir arquitetura
- [ ] Base pronta para adicionar endpoints por dominio

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Estrutura base de pacotes e camadas), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 06 - Tratamento global de excecoes + Problem Details [Dev B]

### Branch
`feature/global-exception-problem-details`

### Objetivo
Centralizar tratamento de erros seguindo RFC 7807 e padroes HTTP obrigatorios.

### Funcionalidades incluidas

- [ ] Implementar `@ControllerAdvice` global
- [ ] Padronizar respostas 400, 401, 403, 404, 409, 422 e erros internos
- [ ] Tratar validacoes Bean Validation com payload consistente

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/exception/`
- [ ] `src/main/java/com/yby/api/dto/` (DTO de erro/problem details)
- [ ] `src/main/java/com/yby/api/config/` (ajustes de serializacao/data)

### Criterios de conclusao

- [ ] Todas as excecoes de API retornam formato Problem Details
- [ ] Status HTTP seguem contrato do AGENTS.md
- [ ] Testes de erro validam estrutura de resposta

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Tratamento global de excecoes e Problem Details RFC 7807), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 07 - Seguranca base com Spring Security e controle de acesso [Dev A]

### Branch
`feature/spring-security-role-access-control`

### Objetivo
Implementar camada de seguranca e autorizacao por perfil para proteger toda a API.

### Funcionalidades incluidas

- [ ] Configurar SecurityFilterChain stateless
- [ ] Liberar somente `POST /api/v1/auth/login` como endpoint publico inicial
- [ ] Aplicar regras de acesso por role `GESTOR` e `SERVIDOR`
- [ ] Configurar CORS restritivo por ambiente

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/security/`
- [ ] `src/main/java/com/yby/api/config/`
- [ ] `src/main/java/com/yby/api/controller/`

### Criterios de conclusao

- [ ] Endpoints protegidos por default
- [ ] Roles aplicadas conforme contratos por modulo
- [ ] Testes de autorizacao cobrindo 401/403

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Seguranca base com Spring Security e controle de acesso por perfil), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 08 - Autenticacao JWT e endpoints de autenticacao [Dev A]

### Branch
`feature/jwt-authentication-endpoints`

### Objetivo
Disponibilizar autenticacao com JWT e gestao basica de identidade do usuario autenticado.

### Funcionalidades incluidas

- [ ] Implementar `POST /api/v1/auth/login` com emissao de JWT (`expiresIn = 28800`)
- [ ] Implementar `GET /api/v1/auth/me`
- [ ] Implementar `PATCH /api/v1/auth/senha` validando senha atual e retornando `204`
- [ ] Garantir BCrypt para senhas e nao retorno de `senha_hash`

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/security/`
- [ ] `src/main/java/com/yby/api/controller/auth/` (ou equivalente)
- [ ] `src/main/java/com/yby/api/service/auth/` (ou equivalente)
- [ ] `src/main/java/com/yby/api/dto/` (`LoginRequestDTO`, `LoginResponseDTO`, etc.)
- [ ] `src/main/java/com/yby/api/entity/usuarios` (ou equivalente)

### Criterios de conclusao

- [ ] Login retorna token valido com exp de 8h
- [ ] `auth/me` retorna dados seguros do usuario
- [ ] Troca de senha exige senha atual e retorna `204 No Content`

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Autenticacao JWT e endpoints de autenticacao), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 09 - CRUD base de usuarios e endpoints de administracao [Dev B]

### Branch
`feature/admin-users-crud`

### Objetivo
Criar estrutura administrativa para gestao de usuarios e operacoes de admin.

### Funcionalidades incluidas

- [ ] Implementar `GET /api/v1/admin/usuarios`
- [ ] Implementar `POST /api/v1/admin/usuarios`
- [ ] Implementar `PATCH /api/v1/admin/usuarios/{id}/status`
- [ ] Aplicar regras: email unico, role valida, usuario ativo/inativo
- [ ] Implementar politica de senha inicial aleatoria e obrigacao de troca no primeiro acesso

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/admin/`
- [ ] `src/main/java/com/yby/api/service/admin/`
- [ ] `src/main/java/com/yby/api/repository/UsuarioRepository.java`
- [ ] `src/main/java/com/yby/api/entity/Usuario.java`
- [ ] `src/main/java/com/yby/api/dto/UsuarioDTO.java`

### Criterios de conclusao

- [ ] Endpoints de admin acessiveis somente por `GESTOR`
- [ ] Fluxo de criacao e desativacao funcional sem exclusao historica
- [ ] Validacoes de unicidade e role cobertas por testes

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (CRUD base de usuarios e endpoints de administracao), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 10 - Endpoints base de municipios [Dev A]

### Branch
`feature/municipios-core-endpoints`

### Objetivo
Entregar os endpoints principais de consulta e manutencao manual de municipios.

### Funcionalidades incluidas

- [ ] Implementar `GET /api/v1/municipios/ranking` com paginacao e ordenacao
- [ ] Implementar `GET /api/v1/municipios/geojson` com filtro opcional por semaforo
- [ ] Implementar `GET /api/v1/municipios/{id}` para detalhe analitico do municipio
- [ ] Implementar `PUT /api/v1/municipios/{id}` para correcao manual (somente `GESTOR`)

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/MunicipioController.java`
- [ ] `src/main/java/com/yby/api/service/MunicipioService.java`
- [ ] `src/main/java/com/yby/api/repository/MunicipioRepository.java`
- [ ] `src/main/java/com/yby/api/dto/MunicipioRankingDTO.java`
- [ ] `src/main/java/com/yby/api/dto/MunicipioDetalheDTO.java`
- [ ] `src/main/java/com/yby/api/mapper/MunicipioMapper.java`

### Criterios de conclusao

- [ ] Ranking retorna `page`, `size`, `totalElements`, `totalPages`, `content`
- [ ] GeoJSON retorna `application/geo+json`
- [ ] Detalhe do municipio inclui score, semaforo, nota de risco, KPI e pendencias
- [ ] `PUT` de municipio restrito a `GESTOR` e preparado para auditoria

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Endpoints base de municipios: ranking, geojson, detalhe e atualizacao manual), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 11 - Calculo de score de prioridade e semaforo [Dev A]

### Branch
`feature/municipios-priority-score`

### Objetivo
Implementar o motor de calculo de prioridade municipal e classificacao visual de risco/prontidao.

### Funcionalidades incluidas

- [ ] Implementar RN-100 a RN-102 (score 0-100, pesos, reponderacao sem fator faltante)
- [ ] Implementar calculo/atualizacao de semaforo (`verde|amarelo|vermelho`)
- [ ] Integrar score ao ranking e ao detalhe do municipio

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/service/score/` (ou equivalente)
- [ ] `src/main/java/com/yby/api/service/MunicipioService.java`
- [ ] `src/main/java/com/yby/api/entity/Municipio.java`
- [ ] `src/main/java/com/yby/api/dto/MunicipioRankingDTO.java`
- [ ] `src/test/java/com/yby/api/service/` (testes de calculo)

### Criterios de conclusao

- [ ] Score sempre no intervalo 0-100
- [ ] Reponderacao aplicada quando fonte estiver indisponivel
- [ ] Testes cobrindo pesos, bordas e consistencia de semaforo

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Calculo de score de prioridade e semaforo), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 12 - Endpoints de indicadores e KPI [Dev B]

### Branch
`feature/indicadores-kpi-endpoints`

### Objetivo
Entregar API de indicadores anuais e KPI de retorno ambiental por gasto publico.

### Funcionalidades incluidas

- [ ] Implementar `GET /api/v1/indicadores/{municipioId}/kpi`
- [ ] Implementar `GET /api/v1/indicadores/{municipioId}/historico`
- [ ] Implementar `POST /api/v1/indicadores` com upsert por (`municipio_id`, `ano`)
- [ ] Implementar RN-103 e RN-104 (calculo de KPI e protecao contra divisao por zero)

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/IndicadorController.java`
- [ ] `src/main/java/com/yby/api/service/IndicadorService.java`
- [ ] `src/main/java/com/yby/api/repository/IndicadorRepository.java`
- [ ] `src/main/java/com/yby/api/entity/Indicador.java`
- [ ] `src/main/java/com/yby/api/dto/KpiDTO.java`
- [ ] `src/main/java/com/yby/api/dto/IndicadorAnualDTO.java`

### Criterios de conclusao

- [ ] KPI retornado sem erro 500 quando `gasto_publico <= 0`
- [ ] Historico anual respeita periodo default do contrato
- [ ] `POST` de indicadores restrito a `GESTOR`

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Endpoints de indicadores e KPI, incluindo historico e cadastro), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 13 - Endpoints de desmatamento (historico e resumo) [Dev B]

### Branch
`feature/desmatamento-query-endpoints`

### Objetivo
Disponibilizar consultas historicas e agregadas de desmatamento por municipio e ano.

### Funcionalidades incluidas

- [ ] Implementar `GET /api/v1/desmatamento/{municipioId}/historico`
- [ ] Implementar `GET /api/v1/desmatamento/resumo`
- [ ] Aplicar filtros por fonte (`PRODES|DETER|ALL`) e intervalo de datas
- [ ] Entregar breakdown agregado por bioma e semaforo no resumo anual

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/DesmatamentoController.java`
- [ ] `src/main/java/com/yby/api/service/DesmatamentoService.java`
- [ ] `src/main/java/com/yby/api/repository/DesmatamentoRepository.java`
- [ ] `src/main/java/com/yby/api/entity/Desmatamento.java`
- [ ] `src/main/java/com/yby/api/dto/DesmatamentoDTO.java`
- [ ] `src/main/java/com/yby/api/dto/DesmatamentoResumoDTO.java`

### Criterios de conclusao

- [ ] Historico retorna dados por fonte e periodo conforme contrato
- [ ] Resumo exige `ano` e retorna total + breakdown esperado
- [ ] Endpoints acessiveis por `GESTOR` e `SERVIDOR`

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Endpoints de desmatamento: historico e resumo agregado), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 14 - Importacao INPE e status de job assincrono [Dev B]

### Branch
`feature/inpe-import-job-status`

### Objetivo
Implementar fluxo assincrono de importacao de desmatamento com rastreabilidade de execucao.

### Funcionalidades incluidas

- [ ] Implementar `POST /api/v1/desmatamento/importar` retornando `jobId` e `202`
- [ ] Implementar `GET /api/v1/desmatamento/importar/{jobId}/status`
- [ ] Suportar status `PROCESSANDO|CONCLUIDO|ERRO`
- [ ] Registrar `registrosInseridos`, `erros[]` e `finalizadoEm`

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/DesmatamentoImportController.java` (ou equivalente)
- [ ] `src/main/java/com/yby/api/service/importacao/`
- [ ] `src/main/java/com/yby/api/integration/inpe/`
- [ ] `src/main/java/com/yby/api/entity/ImportJob.java` (ou equivalente)
- [ ] `src/main/java/com/yby/api/repository/ImportJobRepository.java` (ou equivalente)

### Criterios de conclusao

- [ ] Importacao roda sem bloquear request HTTP
- [ ] Consulta de status reflete progresso real do job
- [ ] Endpoint restrito a `GESTOR`

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Importacao de dados INPE e endpoint de status de job), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 15 - Endpoints de alertas (desperdicio, risco, cadastro manual) [Dev A]

### Branch
`feature/alertas-risk-and-waste`

### Objetivo
Fornecer modulo de alertas para apoio a decisao de investimento e fiscalizacao.

### Funcionalidades incluidas

- [ ] Implementar `GET /api/v1/alertas/desperdicio` aplicando RN-107
- [ ] Implementar `GET /api/v1/alertas/risco/{municipioId}` aplicando RN-108 e RN-109
- [ ] Implementar `POST /api/v1/alertas` para cadastro/atualizacao manual
- [ ] Padronizar severidade e tipo do alerta conforme dominio

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/AlertaController.java`
- [ ] `src/main/java/com/yby/api/service/AlertaService.java`
- [ ] `src/main/java/com/yby/api/repository/AlertaRepository.java`
- [ ] `src/main/java/com/yby/api/entity/Alerta.java`
- [ ] `src/main/java/com/yby/api/dto/AlertaDTO.java`
- [ ] `src/main/java/com/yby/api/dto/RiscoDTO.java`

### Criterios de conclusao

- [ ] Alerta de desperdicio respeita regra de medianas estaduais
- [ ] Analise de risco retorna nota 0-10 e pendencias acionaveis
- [ ] Cadastro manual restrito a `GESTOR`

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Endpoints de alertas de desperdicio, risco pre-investimento e cadastro manual), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 16 - Integracoes externas e jobs agendados com `@Scheduled` [Dev B]

### Branch
`feature/external-integrations-and-schedulers`

### Objetivo
Conectar o back-end com fontes externas do dominio e automatizar cargas periodicas controladas.

### Funcionalidades incluidas

- [ ] Estruturar clients/servicos para INPE, MapBiomas, SICAR, IBGE, SEEG, IBAMA, FUNAI, ICMBio e Portal da Transparencia
- [ ] Implementar jobs agendados com feature flags por ambiente
- [ ] Aplicar regra operacional: PRODES anual e DETER diario
- [ ] Prever modo hackathon com mock/CSV-JSON sem quebrar contrato de API

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/integration/`
- [ ] `src/main/java/com/yby/api/service/integration/`
- [ ] `src/main/java/com/yby/api/job/`
- [ ] `src/main/java/com/yby/api/config/`

### Criterios de conclusao

- [ ] Integracoes encapsuladas em service/integration (nunca no controller)
- [ ] Jobs podem ser habilitados/desabilitados por ambiente
- [ ] Testes nao disparam integracao pesada automaticamente

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Integracoes externas e jobs agendados com Scheduled), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 17 - Logs de auditoria completos [Dev A]

### Branch
`feature/audit-log-tracking`

### Objetivo
Implementar trilha de auditoria para toda escrita realizada por perfil `GESTOR`.

### Funcionalidades incluidas

- [ ] Registrar usuario executor, acao, entidade afetada, timestamp e payload relevante
- [ ] Garantir auditoria em operacoes `POST/PUT/PATCH/DELETE` de `GESTOR`
- [ ] Definir estrategia tecnica (AOP, interceptor ou servico dedicado) para padrao uniforme

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/service/audit/`
- [ ] `src/main/java/com/yby/api/entity/AuditLog.java`
- [ ] `src/main/java/com/yby/api/repository/AuditLogRepository.java`
- [ ] `src/main/java/com/yby/api/config/`
- [ ] `src/main/java/com/yby/api/controller/` (pontos de escrita)

### Criterios de conclusao

- [ ] Escritas de `GESTOR` geram registro em `audit_log`
- [ ] Auditoria disponivel para investigacao e prestacao de contas
- [ ] Falhas de auditoria nao passam silenciosamente

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Logs de auditoria completos para operacoes de escrita do Gestor), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 18 - Geracao de relatorio [Dev B]

### Branch
`feature/admin-report-generation`

### Objetivo
Entregar capacidade de gerar relatorios consolidados para gestao e prestacao de contas.

### Funcionalidades incluidas

- [ ] Implementar `POST /api/v1/admin/relatorio`
- [ ] Definir escopo de relatorio (periodo, municipios, indicadores, alertas, desmatamento, auditoria)
- [ ] Entregar `RelatorioDTO` com estrutura consistente para consumo de front ou exportacao

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/controller/AdminController.java`
- [ ] `src/main/java/com/yby/api/service/RelatorioService.java`
- [ ] `src/main/java/com/yby/api/dto/RelatorioDTO.java`
- [ ] `src/main/java/com/yby/api/repository/` (queries agregadas)

### Criterios de conclusao

- [ ] Endpoint acessivel apenas por `GESTOR`
- [ ] Relatorio agrega dados principais do dominio sem inconsistencias
- [ ] Performance minima aceitavel para datasets esperados

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Geracao de relatorio administrativo), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 19 - Swagger / documentacao da API [Dev B]

### Branch
`chore/swagger-api-documentation`

### Objetivo
Documentar a API de forma navegavel, com contratos claros para consumo interno e externo.

### Funcionalidades incluidas

- [ ] Configurar Springdoc com metadados da API
- [ ] Documentar autenticacao Bearer JWT e requisitos por role
- [ ] Expor exemplos de request/response e modelos de erro RFC 7807
- [ ] Publicar inventario de endpoints por modulo

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/config/SwaggerConfig.java` (ou equivalente)
- [ ] `src/main/java/com/yby/api/controller/`
- [ ] `src/main/java/com/yby/api/dto/`

### Criterios de conclusao

- [ ] Swagger UI funcionando com todos endpoints previstos no AGENTS.md
- [ ] Schemas de DTO alinhados ao comportamento real da API
- [ ] Documentacao de seguranca e erros publicada

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Swagger e documentacao da API), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 20 - Testes automatizados [Dev A + Dev B]

### Branch
`chore/automated-tests-suite`

### Objetivo
Garantir confiabilidade tecnica com cobertura de regras criticas e regressao funcional.

### Funcionalidades incluidas

- [ ] Testes unitarios para score, KPI, desperdicio e risco
- [ ] Testes de seguranca para autenticacao e autorizacao por role
- [ ] Testes de controller para contratos HTTP principais
- [ ] Testes de integracao com banco/Flyway e validacoes de negocio

### Camadas/arquivos envolvidos

- [ ] `src/test/java/com/yby/api/service/`
- [ ] `src/test/java/com/yby/api/controller/`
- [ ] `src/test/java/com/yby/api/security/`
- [ ] `src/test/java/com/yby/api/integration/`

### Criterios de conclusao

- [ ] Cobertura adequada dos fluxos criticos definidos no AGENTS.md
- [ ] Regras RN-100+ validadas por testes automatizados
- [ ] Build CI passa com estabilidade

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Testes automatizados do back-end), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Etapa 21 - Ajustes finais para entrega [Dev A | apoio Dev B]

### Branch
`chore/final-delivery-hardening`

### Objetivo
Fechar o ciclo de entrega com hardening, revisao funcional e checklist de pronto completo.

### Funcionalidades incluidas

- [ ] Revisar aderencia total ao `AGENTS.md` (arquitetura, regras, contratos e seguranca)
- [ ] Revisar configuracoes de producao (CORS, logs, variaveis de ambiente, perfis)
- [ ] Executar smoke tests end-to-end e validar documentacao final
- [ ] Preparar release notes tecnicas e backlog pos-entrega

### Camadas/arquivos envolvidos

- [ ] `src/main/java/com/yby/api/` (revisao transversal)
- [ ] `src/main/resources/` (configs finais)
- [ ] `src/test/java/com/yby/api/` (suite final)
- [ ] `README.md` e docs de operacao

### Criterios de conclusao

- [ ] Projeto compila e testes passam
- [ ] Endpoints, roles, DTOs, migrations e auditoria em conformidade
- [ ] API documentada e pronta para handoff da equipe

### Prompt para PRD da etapa

```text
Leia o AGENTS.md, leia a funcionalidade desta etapa (Ajustes finais para entrega e hardening do sistema), analise o codigo base atual, analise a documentacao existente do projeto e crie um PRD completo para esta funcionalidade. Durante a analise, considere boas praticas de arquitetura, seguranca e boas praticas da OWASP. Utilize a skill Research_Codebase para entender a estrutura atual do projeto antes de propor qualquer implementacao. O PRD deve conter objetivo, escopo, regras de negocio, endpoints, DTOs, entidades envolvidas, validacoes, permissoes, criterios de aceite, riscos tecnicos e plano de implementacao. Salve o PRD na pasta PRDs/ do projeto (caminho relativo ao repositorio).
```

---

## Checklist de validacao do roadmap

- [x] Todas as etapas possuem branch definida
- [x] Todas as etapas possuem prompt de PRD
- [x] Branches seguem padrao consistente (`feature/`, `chore/`, `setup/`)
- [x] Plano alinhado ao `AGENTS.md`
- [x] Ordem de desenvolvimento pensada para trabalho em equipe
- [x] Roadmap separado em setup, seguranca, dominio, integracoes, testes e entrega final


