package co.edu.unbosque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.dto.CambioEstadoRequest;
import co.edu.unbosque.service.api.AdministracionServiceAPI;
import co.edu.unbosque.utils.Utilidad;
import jakarta.validation.Valid;

/**
 * Épica E11 · Administración (HU-41 a HU-45). Solo rol ADMINISTRADOR.
 */
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/admin")
public class AdminRestController {

    @Autowired
    private AdministracionServiceAPI administracionServiceAPI;

    // GET /admin/publicaciones/pendientes — HU-43
    @GetMapping("/publicaciones/pendientes")
    public ResponseEntity<?> pendientes(@RequestParam(required = false) Integer pagina,
                                        @RequestParam(required = false) Integer tamano) {
        return ResponseEntity.ok(administracionServiceAPI.publicacionesPendientes(pagina, tamano));
    }

    // PATCH /admin/publicaciones/{id} — HU-42, HU-43 (APROBAR, PAUSAR, BLOQUEAR, DESBLOQUEAR)
    @PatchMapping("/publicaciones/{id}")
    public ResponseEntity<?> gestionarPublicacion(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(administracionServiceAPI.gestionarPublicacion(Utilidad.idUsuarioActual(auth), id,
                request.getAccion()));
    }

    // GET /admin/usuarios?estado=RESTRINGIDO
    @GetMapping("/usuarios")
    public ResponseEntity<?> usuarios(@RequestParam(required = false) String estado,
                                      @RequestParam(required = false) Integer pagina,
                                      @RequestParam(required = false) Integer tamano) {
        return ResponseEntity.ok(administracionServiceAPI.usuarios(estado, pagina, tamano));
    }

    // PATCH /admin/usuarios/{id} — HU-44 (RESTRINGIR, REACTIVAR)
    @PatchMapping("/usuarios/{id}")
    public ResponseEntity<?> gestionarUsuario(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request,
                                              Authentication auth) {
        return ResponseEntity.ok(administracionServiceAPI.gestionarUsuario(Utilidad.idUsuarioActual(auth), id,
                request.getAccion()));
    }

    // PATCH /admin/resenas/{id}/ocultar — HU-41
    @PatchMapping("/resenas/{id}/ocultar")
    public ResponseEntity<?> ocultarResena(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(administracionServiceAPI.ocultarResena(Utilidad.idUsuarioActual(auth), id));
    }

    // GET /admin/auditoria?entidad=RESERVA&usuarioId=3 — HU-45
    @GetMapping("/auditoria")
    public ResponseEntity<?> auditoria(@RequestParam(required = false) String entidad,
                                       @RequestParam(required = false) Long usuarioId,
                                       @RequestParam(required = false) Integer pagina,
                                       @RequestParam(required = false) Integer tamano) {
        return ResponseEntity.ok(administracionServiceAPI.auditoria(entidad, usuarioId, pagina, tamano));
    }

    // POST /admin/tareas — ejecuta las tareas programadas (para pruebas)
    @PostMapping("/tareas")
    public ResponseEntity<?> tareas() {
        return ResponseEntity.ok(administracionServiceAPI.ejecutarTareasProgramadas());
    }
}
