package cl.duoc.productos.exception;

/**
 * Se lanza cuando los datos son válidos en formato pero no cumplen una regla del negocio (HTTP 400).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
