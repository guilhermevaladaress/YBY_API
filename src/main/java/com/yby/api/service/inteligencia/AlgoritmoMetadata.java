package com.yby.api.service.inteligencia;

/**
 * Versionamento dos algoritmos de decisao (RN-300 - Transparencia e Open Data).
 *
 * <p>Toda saida publica de score/risco/alocacao deve carregar a versao do algoritmo
 * que a produziu, permitindo auditoria publica e reproducao do calculo (Lei 12.527/2011 - LAI).</p>
 */
public final class AlgoritmoMetadata {

    /** Versao corrente do conjunto de regras de inteligencia (RN-101-A .. RN-600). */
    public static final String VERSAO = "inteligencia-2026.05";

    /** Base legal aplicada aos calculos ambientais e fiscais. */
    public static final String BASE_LEGAL =
        "Lei 12.651/2012 (Codigo Florestal); Lei 14.119/2021 (PNPSA); "
            + "Lei 12.527/2011 (LAI); LC 131/2009 (Transparencia); CF art. 37 (eficiencia).";

    private AlgoritmoMetadata() {
    }
}
