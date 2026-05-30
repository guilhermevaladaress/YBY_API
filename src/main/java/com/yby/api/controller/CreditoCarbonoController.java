package com.yby.api.controller;

import com.yby.api.dto.CreditoCarbonoDTO;
import com.yby.api.dto.PageResponseDTO;
import com.yby.api.dto.ProjecaoCarbonoDTO;
import com.yby.api.service.CreditoCarbonoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cadastro de credito rural de carbono e projecao financeira de recebimento (JREDD+).
 *
 * <p>Leitura liberada a GESTOR e SERVIDOR; cadastro/edicao/exclusao restritos a GESTOR (auditado).</p>
 */
@RestController
@RequestMapping("/api/v1/creditos-carbono")
public class CreditoCarbonoController {

    private final CreditoCarbonoService creditoCarbonoService;

    public CreditoCarbonoController(CreditoCarbonoService creditoCarbonoService) {
        this.creditoCarbonoService = creditoCarbonoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<PageResponseDTO<CreditoCarbonoDTO>> listar(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponseDTO.from(creditoCarbonoService.listar(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<CreditoCarbonoDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(creditoCarbonoService.buscar(id));
    }

    @GetMapping("/municipio/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<CreditoCarbonoDTO>> porMunicipio(@PathVariable Long municipioId) {
        return ResponseEntity.ok(creditoCarbonoService.listarPorMunicipio(municipioId));
    }

    @GetMapping("/{id}/projecao")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<ProjecaoCarbonoDTO> projecao(@PathVariable Long id) {
        return ResponseEntity.ok(creditoCarbonoService.projetar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<CreditoCarbonoDTO> criar(@Valid @RequestBody CreditoCarbonoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditoCarbonoService.criar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<CreditoCarbonoDTO> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody CreditoCarbonoDTO dto) {
        return ResponseEntity.ok(creditoCarbonoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        creditoCarbonoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
