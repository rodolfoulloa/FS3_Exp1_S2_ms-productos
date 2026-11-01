package cl.duoc.productos.service;

import cl.duoc.productos.dto.ProductoRequest;
import cl.duoc.productos.dto.ProductoResponse;
import cl.duoc.productos.dto.UsuarioRemoto;
import cl.duoc.productos.exception.ConflictoException;
import cl.duoc.productos.exception.RecursoNoEncontradoException;
import cl.duoc.productos.exception.ReglaNegocioException;
import cl.duoc.productos.model.Categoria;
import cl.duoc.productos.model.Producto;
import cl.duoc.productos.repository.CategoriaRepository;
import cl.duoc.productos.repository.ProductoRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AutorizacionService autorizacion;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                               AutorizacionService autorizacion) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.autorizacion = autorizacion;
    }

    // ===================== Consultas (públicas) =====================

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, Boolean activo) {
        List<Producto> productos;
        if (categoriaId != null && activo != null) {
            productos = productoRepository.findByCategoriaIdAndActivoOrderByNombreAsc(categoriaId, activo);
        } else if (categoriaId != null) {
            productos = productoRepository.findByCategoriaIdOrderByNombreAsc(categoriaId);
        } else if (activo != null) {
            productos = productoRepository.findByActivoOrderByNombreAsc(activo);
        } else {
            productos = productoRepository.findAll(Sort.by("id"));
        }
        return productos.stream().map(ProductoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return ProductoResponse.desde(buscarEntidad(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("Debe indicar un texto de búsqueda (parámetro q)");
        }
        return productoRepository.buscarPorTexto(texto.trim()).stream().map(ProductoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> porRangoDePrecio(Integer min, Integer max) {
        if (min < 0 || max < 0 || min > max) {
            throw new ReglaNegocioException("Rango de precios inválido: min debe ser >= 0 y menor o igual a max");
        }
        return productoRepository.findByPrecioBetweenOrderByPrecioAsc(min, max).stream()
                .map(ProductoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> stockBajo(Integer umbral) {
        if (umbral < 0) {
            throw new ReglaNegocioException("El umbral de stock no puede ser negativo");
        }
        return productoRepository.findByStockLessThanEqualOrderByStockAsc(umbral).stream()
                .map(ProductoResponse::desde).toList();
    }

    // ===================== Gestión (requiere rol) =====================

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest req, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN, AutorizacionService.VENDEDOR);
        if (productoRepository.existsBySkuIgnoreCase(req.sku())) {
            throw new ConflictoException("Ya existe un producto con SKU " + req.sku());
        }
        Producto p = new Producto();
        copiarDatos(req, p);
        p.setActivo(req.activo() == null ? Boolean.TRUE : req.activo());
        Producto creado = productoRepository.save(p);
        log.info("Producto {} creado por {} (ID {})", creado.getSku(), u.nombreUsuario(), creado.getId());
        return ProductoResponse.desde(creado);
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN, AutorizacionService.VENDEDOR);
        Producto p = buscarEntidad(id);
        if (productoRepository.existsBySkuIgnoreCaseAndIdNot(req.sku(), id)) {
            throw new ConflictoException("Ya existe otro producto con SKU " + req.sku());
        }
        copiarDatos(req, p);
        if (req.activo() != null) {
            p.setActivo(req.activo());
        }
        log.info("Producto ID {} actualizado por {}", id, u.nombreUsuario());
        return ProductoResponse.desde(productoRepository.save(p));
    }

    @Override
    @Transactional
    public ProductoResponse ajustarStock(Long id, Integer cantidad, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN, AutorizacionService.VENDEDOR);
        Producto p = buscarEntidad(id);
        int nuevoStock = p.getStock() + cantidad;
        if (nuevoStock < 0) {
            throw new ReglaNegocioException("Stock insuficiente: el producto tiene " + p.getStock()
                    + " unidades y se intentó descontar " + Math.abs(cantidad));
        }
        p.setStock(nuevoStock);
        log.info("Stock de producto ID {} ajustado en {} por {} (nuevo stock {})",
                id, cantidad, u.nombreUsuario(), nuevoStock);
        return ProductoResponse.desde(productoRepository.save(p));
    }

    @Override
    @Transactional
    public ProductoResponse cambiarEstado(Long id, boolean activo, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN, AutorizacionService.VENDEDOR);
        Producto p = buscarEntidad(id);
        p.setActivo(activo);
        log.info("Producto ID {} {} por {}", id, activo ? "activado" : "desactivado", u.nombreUsuario());
        return ProductoResponse.desde(productoRepository.save(p));
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        UsuarioRemoto u = autorizacion.exigirRol(usuarioId, AutorizacionService.ADMIN);
        Producto p = buscarEntidad(id);
        productoRepository.delete(p);
        log.info("Producto ID {} eliminado por {}", id, u.nombreUsuario());
    }

    private Producto buscarEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con ID " + id));
    }

    private void copiarDatos(ProductoRequest req, Producto p) {
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una categoría con ID " + req.categoriaId()));
        p.setSku(req.sku().trim().toUpperCase());
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion());
        p.setPrecio(req.precio());
        p.setStock(req.stock());
        p.setCategoria(categoria);
    }
}
