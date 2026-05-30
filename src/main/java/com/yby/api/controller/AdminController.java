package com.yby.api.controller;

import com.yby.api.dto.CreateUsuarioDTO;
import com.yby.api.dto.PageResponseDTO;
import com.yby.api.dto.RelatorioDTO;
import com.yby.api.dto.SyncResultDTO;
import com.yby.api.dto.UsuarioDTO;
import com.yby.api.dto.UsuarioStatusPatchDTO;
import com.yby.api.service.AdminService;
import com.yby.api.service.IbgeSyncService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('GESTOR')")
public class AdminController {

    private final AdminService adminService;
    private final IbgeSyncService ibgeSyncService;

    public AdminController(AdminService adminService, IbgeSyncService ibgeSyncService) {
        this.adminService = adminService;
        this.ibgeSyncService = ibgeSyncService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<PageResponseDTO<UsuarioDTO>> usuarios(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponseDTO.from(adminService.listarUsuarios(pageable)));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDTO> criarUsuario(@Valid @RequestBody CreateUsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.criarUsuario(dto));
    }

    @PatchMapping("/usuarios/{id}/status")
    public ResponseEntity<UsuarioDTO> atualizarStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioStatusPatchDTO dto) {
        return ResponseEntity.ok(adminService.atualizarStatus(id, dto));
    }

    @PostMapping("/relatorio")
    public ResponseEntity<RelatorioDTO> relatorio() {
        return ResponseEntity.ok(adminService.gerarRelatorioExecutivo());
    }

    /**
     * Sincroniza a lista oficial de municipios via API publica do IBGE (AGENTS.md 5.4).
     * Operacao de escrita por GESTOR: auditada (RN-007).
     */
    @PostMapping("/sync/ibge/municipios")
    public ResponseEntity<SyncResultDTO> sincronizarMunicipiosIbge(
        @RequestParam(required = false) String uf) {
        return ResponseEntity.ok(ibgeSyncService.sincronizarMunicipios(uf));
    }
}
