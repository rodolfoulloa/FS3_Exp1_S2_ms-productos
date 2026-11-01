package cl.duoc.productos.exception;

/**
 * Se lanza cuando la operación choca con datos existentes, por ejemplo un valor duplicado (HTTP 409).
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
