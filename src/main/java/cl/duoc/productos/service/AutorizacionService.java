package cl.duoc.productos.service;

import cl.duoc.productos.client.UsuarioClient;
import cl.duoc.productos.dto.UsuarioRemoto;
import cl.duoc.productos.exception.AccesoDenegadoException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Valida que quien realiza una operación de gestión tenga el rol necesario.
 * El usuario se identifica con el header X-Usuario-Id (ID obtenido en el login de ms-usuarios).
 *
 * Privilegios:
 *   ADMIN    -> todo: categorías y productos (crear, modificar, eliminar).
 *   VENDEDOR -> crear y modificar productos, ajustar stock.
 *   CLIENTE  -> solo consulta (GET).
 */
@Service
public class AutorizacionService {

    public static final String ADMIN = "ADMIN";
    public static final String VENDEDOR = "VENDEDOR";

    private static final Logger log = LoggerFactory.getLogger(AutorizacionService.class);

    private final UsuarioClient usuarioClient;

    public AutorizacionService(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    public UsuarioRemoto exigirRol(Long usuarioId, String... rolesPermitidos) {
        UsuarioRemoto usuario = usuarioClient.obtenerUsuario(usuarioId);
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new AccesoDenegadoException("El usuario " + usuario.nombreUsuario() + " está desactivado");
        }
        boolean permitido = Arrays.stream(rolesPermitidos).anyMatch(r -> r.equalsIgnoreCase(usuario.rol()));
        if (!permitido) {
            log.warn("Acceso denegado a {} (rol {}). Roles permitidos: {}",
                    usuario.nombreUsuario(), usuario.rol(), Arrays.toString(rolesPermitidos));
            throw new AccesoDenegadoException("El usuario " + usuario.nombreUsuario() + " (rol " + usuario.rol()
                    + ") no tiene permiso. Roles permitidos: " + String.join(", ", rolesPermitidos));
        }
        return usuario;
    }
}
