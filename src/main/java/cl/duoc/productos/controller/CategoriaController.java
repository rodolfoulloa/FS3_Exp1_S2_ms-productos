package cl.duoc.productos.controller;

import cl.duoc.productos.dto.ProductoResponse;
import cl.duoc.productos.dto.ResumenCategoriaResponse;
import cl.duoc.productos.model.Categoria;
import cl.duoc.productos.service.CategoriaService;
import cl.duoc.productos.service.ProductoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Categorías de productos. Consultar es público; crear, modificar y eliminar requiere rol ADMIN
 * (header X-Usuario-Id).
 */
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private static final String HEADER_USUARIO = "X-Usuario-Id";

    private final CategoriaService categoriaService;
    private final ProductoService productoService;

    public CategoriaController(CategoriaService categoriaService, ProductoService productoService) {
        this.categoriaService = categoriaService;
        this.productoService = productoService;
    }

    // GET /categorias
    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listar();
    }

    // GET /categorias/{id}
    @GetMapping("/{id}")
    public Categoria obtener(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    // GET /categorias/{id}/productos -> productos de la categoría
    @GetMapping("/{id}/productos")
    public List<ProductoResponse> productos(@PathVariable Long id) {
        categoriaService.obtenerPorId(id);
        return productoService.listar(id, null);
    }

    // GET /categorias/resumen -> cantidad de productos, stock y valor de inventario por categoría
    @GetMapping("/resumen")
    public List<ResumenCategoriaResponse> resumen() {
        return categoriaService.resumen();
    }

    // POST /categorias (ADMIN)
    @PostMapping
    public ResponseEntity<Categoria> crear(@RequestHeader(HEADER_USUARIO) Long usuarioId,
                                           @Valid @RequestBody Categoria categoria) {
        Categoria creada = categoriaService.crear(categoria, usuarioId);
        return ResponseEntity.created(URI.create("/categorias/" + creada.getId())).body(creada);
    }

    // PUT /categorias/{id} (ADMIN)
    @PutMapping("/{id}")
    public Categoria actualizar(@RequestHeader(HEADER_USUARIO) Long usuarioId, @PathVariable Long id,
                                @Valid @RequestBody Categoria categoria) {
        return categoriaService.actualizar(id, categoria, usuarioId);
    }

    // DELETE /categorias/{id} (ADMIN, solo si no tiene productos)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(HEADER_USUARIO) Long usuarioId, @PathVariable Long id) {
        categoriaService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
