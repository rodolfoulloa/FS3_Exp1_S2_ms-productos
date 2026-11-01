package cl.duoc.productos.exception;

/**
 * Se lanza cuando no es posible comunicarse con otro microservicio (HTTP 503).
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
