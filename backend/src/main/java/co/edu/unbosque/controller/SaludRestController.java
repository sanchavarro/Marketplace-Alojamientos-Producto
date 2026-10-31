package co.edu.unbosque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.service.api.SaludServiceAPI;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/salud")
public class SaludRestController {

    @Autowired
    private SaludServiceAPI saludServiceAPI;

    // GET /salud — estado del sistema (público)
    @GetMapping
    public ResponseEntity<?> estado() {
        return ResponseEntity.ok(saludServiceAPI.estado());
    }

    // GET /salud/metricas — métricas de observabilidad (administrador)
    @GetMapping("/metricas")
    public ResponseEntity<?> metricas() {
        return ResponseEntity.ok(saludServiceAPI.metricas());
    }
}
