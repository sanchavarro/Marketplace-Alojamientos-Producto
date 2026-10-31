package co.edu.unbosque.controller;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.service.api.ReporteServiceAPI;
import co.edu.unbosque.utils.Utilidad;

/**
 * Épicas E12 · Dashboard (HU-46) y E13 · Reportes (HU-47, HU-48). Solo rol ADMINISTRADOR.
 */
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/reportes")
public class ReporteRestController {

    @Autowired
    private ReporteServiceAPI reporteServiceAPI;

    // GET /reportes/dashboard — HU-46
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(reporteServiceAPI.dashboard());
    }

    // GET /reportes/reservas?formato=csv|pdf&desde=2026-11-01&hasta=2026-11-30 — HU-47, HU-48
    @GetMapping("/reservas")
    public ResponseEntity<?> exportar(@RequestParam(defaultValue = "csv") String formato,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                      Authentication auth) {
        byte[] archivo = reporteServiceAPI.exportarReservas(Utilidad.idUsuarioActual(auth), formato, desde, hasta);
        boolean pdf = "pdf".equalsIgnoreCase(formato);
        String nombre = "reservas_" + LocalDate.now() + (pdf ? ".pdf" : ".csv");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(pdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(archivo);
    }
}
