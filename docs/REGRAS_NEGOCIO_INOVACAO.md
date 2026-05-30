# Regras de Negócio Inovadoras — Gestão Pública Ambiental (JREDD+ Intelligence)

Este documento complementa o `AGENTS.md` com **extensões inovadoras** para a gestão pública ambiental.
As regras abaixo **não substituem** as RN-001+ do `AGENTS.md`; elas **ampliam** a tomada de decisão com foco em
priorização preventiva, eficiência do gasto público e transparência.

> Status: propostas de regra de negócio (podem ser gradualmente implementadas no `service`).

---

## 1) Score de Prioridade com Tendência (RN-101-A)

**Objetivo:** capturar municípios em aceleração de desmatamento antes que o problema se agrave.

**Regra:**
- Calcular tendência anual:
  - $t = \frac{desmatamento_{ano} - desmatamento_{ano-1}}{desmatamento_{ano-1}}$
- Ajustar score:
  - se $t > 10\%$: **+15** pontos
  - se $t < -10\%$: **-10** pontos
  - caso contrário: **0**

**Saídas esperadas:**
- `tendenciaDesmatamento` (percentual)
- `statusTendencia` (`ACELERANDO|ESTAVEL|MELHORANDO`)

---

## 2) KPI Multidimensional (RN-103-B)

**Objetivo:** incorporar impacto social e eficiência fiscal.

**Regra:**
\[
KPI = (resultado\_ambiental \times w_a) + (indicador\_social \times w_s) + (eficiencia\_fiscal \times w_f)
\]

**Indicadores sociais mínimos:**
- empregos em conservação
- famílias beneficiadas por PSA
- comunidades tradicionais envolvidas

**Bônus:** se houver investimentos sociais relevantes, aplicar bônus de **5–15%**.

---

## 3) Semáforo Bioma-Sensível (RN-106-A)

**Objetivo:** calibrar a prontidão por bioma.

**Exemplo de thresholds:**
- **Amazonia:** verde ≥ 75; amarelo 50–74; vermelho < 50
- **Cerrado:** verde ≥ 60; amarelo 40–59; vermelho < 40
- **Pantanal:** verde ≥ 70 **e** baixa vulnerabilidade hídrica

**Saídas esperadas:**
- `biomaPrincipal`
- `justificativaSemaforo`

---

## 4) Risco Preditivo com Histórico (RN-108-A)

**Objetivo:** transformar risco em ação preventiva.

**Composição mínima (0–10):**
- Embargos IBAMA
- Sobreposição TI/FUNAI
- Sobreposição UC/ICMBio
- Irregularidades CAR

**Fator tempo:**
- embargo nos últimos 6 meses: **× 1,2**
- sem embargo há 2 anos: **× 0,8**
- plano de ação ativo: **× 0,9**

---

## 5) Alerta de Desperdício Inteligente (RN-107-A)

**Objetivo:** detectar ineficiência relativa (benchmarking).

**Regra:**
- gasto acima da mediana **e** resultado ambiental abaixo da mediana
- comparar com municípios similares (bioma e porte)
- se gasto/resultado > 2× média do grupo → alerta vermelho

---

## 6) Otimização de Alocação (RN-200)

**Objetivo:** sugerir alocação ótima de orçamento público.

**Entrada:** orçamento, ano, restrições, estratégia (`maximo_kpi`, `equidade_regional`, `risco_minimo`).

**Saída:** lista de municípios com valores alocados, retorno esperado, impacto social e justificativas.

---

## 7) Transparência e Open Data (RN-300)

**Objetivo:** permitir auditoria pública dos dados e cálculos.

**Regras:**
- endpoints públicos somente leitura (ranking e geojson)
- registrar versão do algoritmo de score
- metadados de atualização visíveis ao cidadão

---

## 8) Equidade Territorial (RN-500)

**Objetivo:** evitar concentração do investimento em poucos municípios.

**Regra:**
- calcular índice de equidade ($1 - \frac{desvio}{média}$)
- se < 0,3: aplicar bônus de score para municípios abaixo da mediana

---

## 9) Freshness e Revalidação (RN-600)

**Objetivo:** impedir decisões com dados obsoletos.

**Regra:**
- se dados críticos sem atualização > 6 meses: exigir revisão
- se dados PRODES > 12 meses: semáforo vira amarelo

---

## 10) Auditoria Ambiental Estendida (RN-007-A)

**Objetivo:** rastrear decisões e mudanças manuais.

**Obrigatório registrar:**
- antes/depois da alteração
- fonte da alteração (manual, integração, recálculo)
- justificativa

---

## Observação

As regras acima devem ser implementadas preferencialmente em `service`, com DTOs dedicados e testes unitários.
