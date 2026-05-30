package com.yby.api.controller;

import com.yby.api.dto.CarbonoHistoricoDTO;
import com.yby.api.dto.CarbonoRegistroDTO;
import com.yby.api.service.CarbonoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carbono")
public class CarbonoController {

    private final CarbonoService carbonoService;

    public CarbonoController(CarbonoService carbonoService) {
        this.carbonoService = carbonoService;
    }

    @GetMapping("/historico/menores")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<CarbonoHistoricoDTO>> historicoMenores(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        @RequestParam(defaultValue = "20") Integer limite
    ) {
        return ResponseEntity.ok(carbonoService.historicoMenores(dataInicio, dataFim, limite));
    }

    @GetMapping("/historico/menores/relatorio/pdf")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<byte[]> relatorioPdf(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        @RequestParam(defaultValue = "20") Integer limite
    ) {
        byte[] pdf = carbonoService.relatorioPdfMenores(dataInicio, dataFim, limite);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("relatorio-carbono.pdf").build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PostMapping("/registros")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<CarbonoRegistroDTO> registrar(@Valid @RequestBody CarbonoRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carbonoService.registrar(dto));
    }
}
