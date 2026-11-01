package cl.duoc.productos.client;

import cl.duoc.productos.dto.UsuarioRemoto;
import cl.duoc.productos.exception.RecursoNoEncontradoException;
import cl.duoc.productos.exception.ServicioNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente REST para comunicarse con el microservicio ms-usuarios.
 * Cada microservicio es dueño de sus tablas: ms-productos no lee la tabla USUARIOS,
 * sino que le pregunta a ms-usuarios a través de su API.
 */
@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);

    private final RestClient restClient;

    public UsuarioClient(RestClient.Builder builder, @Value("${usuarios.service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public UsuarioRemoto obtenerUsuario(Long id) {
        try {
            log.debug("Consultando a ms-usuarios por el usuario ID {}", id);
            return restClient.get()
                    .uri("/usuarios/{id}", id)
                    .retrieve()
                    .body(UsuarioRemoto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("No existe un usuario con ID " + id);
        } catch (ResourceAccessException e) {
            log.error("ms-usuarios no responde: {}", e.getMessage());
            throw new ServicioNoDisponibleException("El microservicio de usuarios no está disponible", e);
        } catch (RestClientException e) {
            log.error("Error al consultar ms-usuarios: {}", e.getMessage());
            throw new ServicioNoDisponibleException("Error al comunicarse con el microservicio de usuarios", e);
        }
    }
}
