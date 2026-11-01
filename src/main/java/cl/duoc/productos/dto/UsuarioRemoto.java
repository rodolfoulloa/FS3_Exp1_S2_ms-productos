package cl.duoc.productos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Datos del usuario que entrega ms-usuarios (GET /usuarios/{id}).
 * Solo se toman los campos necesarios para validar permisos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioRemoto(Long id, String nombreUsuario, String rol, Boolean activo) {
}
