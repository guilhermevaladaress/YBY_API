# AGENTS.md - JREDD+ Intelligence Back-End

## Objetivo do projeto

Este repositório contém o back-end do JREDD+ Intelligence, responsável por expor uma API REST para o front-end, aplicar regras de negócio, calcular indicadores ambientais, consultar dados por município, autenticar usuários e integrar fontes externas como INPE, MapBiomas, SICAR, IBGE e bases estaduais.

O projeto deve priorizar clareza, organização, segurança e rapidez de entrega, pois será usado em contexto de hackathon.

## Stack principal

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT
* PostgreSQL, preferencialmente Supabase free
* Flyway para migrations
* Springdoc / Swagger UI
* Bean Validation
* Lombok, se já estiver configurado no projeto

Não trocar a stack sem necessidade.

## Arquitetura esperada

Organizar o código em camadas:

* `controller` ou `resource`: recebe requisições HTTP e retorna DTOs
* `service`: concentra regras de negócio
* `repository`: acesso ao banco via Spring Data JPA
* `entity` ou `model`: entidades JPA
* `dto`: objetos de entrada e saída
* `mapper`: conversões entre entidades e DTOs, se necessário
* `security`: JWT, filtros, autenticação e autorização
* `config`: configurações gerais
* `exception`: tratamento global de erros
* `integration`: serviços de integração externa
* `job` ou `scheduler`: cargas agendadas
* `migration`: scripts Flyway em `src/main/resources/db/migration`

Evitar regra de negócio dentro de controller.

## Convenções de API

Todas as rotas devem seguir o prefixo:

`/api/v1`

Usar os status HTTP corretamente:

* `200 OK` para consultas e atualizações com retorno
* `201 CREATED` para criação
* `202 ACCEPTED` para processamento assíncrono
* `204 NO CONTENT` para operações sem corpo de resposta
* `400 BAD REQUEST` para erro de requisição
* `401 UNAUTHORIZED` para ausência ou falha de autenticação
* `403 FORBIDDEN` para usuário sem permissão
* `404 NOT FOUND` para recurso inexistente
* `409 CONFLICT` para conflito, como e-mail duplicado
* `422 UNPROCESSABLE ENTITY` para validações de negócio

Erros devem seguir o padrão RFC 7807 Problem Details quando possível.

## Autenticação e autorização

O sistema possui dois perfis:

* `GESTOR`
* `SERVIDOR`

No Spring Security, usar roles:

* `ROLE_GESTOR`
* `ROLE_SERVIDOR`

Regras:

* `GESTOR` pode consultar dados, cadastrar usuários, importar bases, atualizar dados, administrar alertas e gerar relatórios.
* `SERVIDOR` pode apenas consultar mapa, ranking, detalhe de município, histórico de desmatamento e relatórios permitidos.

Usar anotações como:

```java
@PreAuthorize("hasRole('GESTOR')")
@PreAuthorize("hasAnyRole('GESTOR', 'SERVIDOR')")
```

O JWT deve ser stateless e enviado no header:

```http
Authorization: Bearer <token>
```

## Endpoints principais

Implementar seguindo os contratos planejados:

### Municípios

* `GET /api/v1/municipios/ranking`
* `GET /api/v1/municipios/geojson`
* `GET /api/v1/municipios/{id}`
* `PUT /api/v1/municipios/{id}` somente `GESTOR`

### Indicadores e KPI

* `GET /api/v1/indicadores/{municipioId}/kpi`
* `GET /api/v1/indicadores/{municipioId}/historico`
* `POST /api/v1/indicadores` somente `GESTOR`

### Desmatamento

* `GET /api/v1/desmatamento/{municipioId}/historico`
* `GET /api/v1/desmatamento/resumo`
* `POST /api/v1/desmatamento/importar` somente `GESTOR`
* `GET /api/v1/desmatamento/importar/{jobId}/status` somente `GESTOR`

### Alertas

* `GET /api/v1/alertas/desperdicio`
* `GET /api/v1/alertas/risco/{municipioId}`
* `POST /api/v1/alertas` somente `GESTOR`

### Autenticação

* `POST /api/v1/auth/login`
* `GET /api/v1/auth/me`
* `PATCH /api/v1/auth/senha`

### Administração

* `GET /api/v1/admin/usuarios` somente `GESTOR`
* `POST /api/v1/admin/usuarios` somente `GESTOR`
* `PATCH /api/v1/admin/usuarios/{id}/status` somente `GESTOR`
* `POST /api/v1/admin/relatorio` somente `GESTOR`

## Modelo de dados base

Considerar como tabelas principais:

* `municipios`
* `indicadores`
* `desmatamento`
* `alertas`
* `usuarios`
* `audit_log`, para auditoria de escritas

Campos importantes:

### municipios

* `id`
* `codigo_ibge`
* `nome`
* `area_ha`
* `score_prioridade`
* `semaforo`
* `geojson_polygon`

### indicadores

* `id`
* `municipio_id`
* `ano`
* `gasto_publico`
* `resultado_ambiental`
* `kpi_retorno`

### desmatamento

* `id`
* `municipio_id`
* `data`
* `area_desmatada_ha`
* `fonte`

### alertas

* `id`
* `municipio_id`
* `tipo`
* `gravidade`
* `ativo`

### usuarios

* `id`
* `email`
* `senha_hash`
* `role`
* `ativo`

## Regras de negócio importantes

O score de prioridade do município vai de 0 a 100 e deve considerar, quando os dados existirem:

* desmatamento recente: 40%
* eficiência de gasto: 30%
* irregularidades CAR: 20%
* área elegível: 10%

O KPI de retorno deve seguir a ideia:

```text
kpiRetorno = resultadoAmbiental / gastoPublico
```

Evitar divisão por zero. Quando `gastoPublico` for zero ou nulo, tratar de forma segura.

O semáforo deve ser representado por:

* `verde`
* `amarelo`
* `vermelho`

## DTOs e validação

Não expor entidades JPA diretamente nos controllers.

Criar DTOs para entrada e saída, por exemplo:

* `MunicipioRankingDTO`
* `MunicipioDetalheDTO`
* `KpiDTO`
* `IndicadorAnualDTO`
* `DesmatamentoDTO`
* `DesmatamentoResumoDTO`
* `AlertaDTO`
* `RiscoDTO`
* `UsuarioDTO`
* `RelatorioDTO`
* `LoginRequestDTO`
* `LoginResponseDTO`

Usar Bean Validation em DTOs de entrada:

* `@NotNull`
* `@NotBlank`
* `@Email`
* `@PositiveOrZero`
* `@Min`
* `@Max`

## Banco de dados e migrations

Usar Flyway.

Criar migrations em:

```text
src/main/resources/db/migration
```

Seguir o padrão:

```text
V1__create_initial_tables.sql
V2__insert_seed_data.sql
```

Não alterar migrations antigas depois de aplicadas. Criar nova migration para mudanças.

## Segurança

Nunca salvar senha em texto puro.

Usar BCrypt para `senha_hash`.

Não retornar `senha_hash` em nenhum DTO.

Endpoints administrativos devem exigir `GESTOR`.

Endpoints públicos devem ser mínimos. Inicialmente, apenas login deve ser público.

## Auditoria

Toda operação de escrita feita por `GESTOR` deve ser preparada para gerar log de auditoria:

* usuário
* ação
* entidade afetada
* timestamp
* payload relevante

Caso a auditoria completa não seja implementada de início, deixar estrutura preparada para evolução.

## Integrações externas

Implementar integrações como services isolados, nunca dentro dos controllers.

Exemplos:

* `InpeTerraBrasilisService`
* `MapBiomasService`
* `SicarService`
* `IbgeGeoService`
* `SeegService`

Durante o hackathon, se a integração real for demorada, criar serviços com dados mockados ou carga manual por CSV/JSON, mas manter a estrutura limpa para troca futura.

## Jobs agendados

Cargas periódicas devem ficar em classes próprias usando `@Scheduled`.

Exemplo:

```java
@Scheduled(cron = "0 0 6 * * *")
public void cargaDiariaDeter() {
    // carga diária
}
```

Não executar integrações pesadas automaticamente em ambiente de teste.

## Swagger

Manter a documentação da API disponível em desenvolvimento via Swagger UI.

Documentar controllers com nomes e descrições claras quando possível.

## Estilo de código

* Código simples e direto.
* Nomes em português são permitidos para domínio do projeto.
* Evitar abreviações confusas.
* Não criar abstrações desnecessárias.
* Não duplicar regra de negócio.
* Preferir métodos pequenos e legíveis.
* Não misturar responsabilidades.
* Evitar comentários óbvios; comentar apenas regra de negócio relevante.

## Testes

Quando criar ou alterar regra importante, adicionar testes quando viável.

Priorizar testes para:

* services de cálculo de score
* KPI
* autenticação
* autorização por perfil
* endpoints principais
* validações de DTO

Se o projeto já tiver padrão de testes, seguir o padrão existente.

## Antes de finalizar uma tarefa

Antes de considerar uma tarefa pronta:

1. Verificar se o projeto compila.
2. Verificar se não há erro óbvio de importação.
3. Conferir se endpoints seguem `/api/v1`.
4. Conferir se permissões `GESTOR` e `SERVIDOR` foram aplicadas corretamente.
5. Conferir se DTOs não expõem dados sensíveis.
6. Conferir se migrations estão no padrão Flyway.
7. Conferir se o código novo segue a arquitetura em camadas.

## Como trabalhar neste projeto

Ao receber uma tarefa:

1. Leia este `AGENTS.md`.
2. Verifique a estrutura atual do projeto antes de criar arquivos novos.
3. Siga os padrões já existentes.
4. Faça mudanças pequenas e coesas.
5. Explique no final o que foi alterado.
6. Informe qualquer pendência ou suposição feita.

Não invente endpoints fora do escopo sem avisar.
Não alterar decisões principais de arquitetura sem motivo claro.
Não remover código existente sem confirmar que ele não é usado.
