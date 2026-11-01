package cl.duoc.productos.exception;

/**
 * Se lanza cuando el usuario no tiene el rol necesario para la operación (HTTP 403).
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
