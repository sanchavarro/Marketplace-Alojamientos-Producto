package co.edu.unbosque.config;

import java.io.IOException;
import java.util.logging.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import co.edu.unbosque.utils.MetricasSistema;
import co.edu.unbosque.utils.Utilidad;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HU-49 Logs estructurados y observabilidad (T8 Monitor).
 * A cada petición le asigna un id de correlación (encabezado X-Correlation-Id) y escribe una línea
 * clave=valor con método, ruta, estado, duración y usuario. También alimenta las métricas (Singleton).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroCorrelacion extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger("acceso");
    public static final String ENCABEZADO = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlacion = request.getHeader(ENCABEZADO);
        if (correlacion == null || correlacion.isBlank() || correlacion.length() > 64) {
            correlacion = Utilidad.generarIdCorrelacion();
        }
        response.setHeader(ENCABEZADO, correlacion);
        long inicio = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duracion = System.currentTimeMillis() - inicio;
            Object usuario = request.getAttribute(JwtFiltro.ATRIBUTO_USUARIO);
            MetricasSistema.getInstancia().registrarPeticion(response.getStatus(), duracion);
            logger.info("correlacion=" + correlacion
                    + " metodo=" + request.getMethod()
                    + " ruta=" + request.getRequestURI()
                    + " estado=" + response.getStatus()
                    + " duracionMs=" + duracion
                    + " usuario=" + (usuario == null ? "anonimo" : usuario));
        }
    }
}
