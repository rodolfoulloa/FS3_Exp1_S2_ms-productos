package cl.duoc.productos.service;

import cl.duoc.productos.dto.ResumenCategoriaResponse;
import cl.duoc.productos.dto.UsuarioRemoto;
import cl.duoc.productos.exception.ConflictoException;
import cl.duoc.productos.exception.RecursoNoEncontradoException;
import cl.duoc.productos.model.Categoria;
import cl.duoc.productos.repository.CategoriaRepository;
import cl.duoc.productos.repository.ProductoRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final AutorizacionService autorizacion;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                                AutorizacionService autorizacion) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.autorizacion = autorizacion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAll(Sort.by("nombre"));
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una categoría con ID " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenCategoriaResponse> resumen() {
        return categoriaRepository.resumen();
    }

    @Override
    @Transactional
    public Categoria crear(Categoria categoria, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN);
        categoria.setId(null);
        categoria.setNombre(categoria.getNombre().trim());
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new ConflictoException("Ya existe la categoría " + categoria.getNombre());
        }
        Categoria creada = categoriaRepository.save(categoria);
        log.info("Categoría {} creada por {} (ID {})", creada.getNombre(), u.nombreUsuario(), creada.getId());
        return creada;
    }

    @Override
    @Transactional
    public Categoria actualizar(Long id, Categoria datos, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN);
        Categoria categoria = obtenerPorId(id);
        String nombre = datos.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe la categoría " + nombre);
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.getDescripcion());
        log.info("Categoría ID {} actualizada por {}", id, u.nombreUsuario());
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN);
        Categoria categoria = obtenerPorId(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar la categoría " + categoria.getNombre()
                    + " porque tiene productos asociados");
        }
        categoriaRepository.delete(categoria);
        log.info("Categoría ID {} eliminada por {}", id, u.nombreUsuario());
    }
}
