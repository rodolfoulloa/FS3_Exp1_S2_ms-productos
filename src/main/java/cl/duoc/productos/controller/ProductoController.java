package cl.duoc.productos.controller;

import cl.duoc.productos.dto.ProductoRequest;
import cl.duoc.productos.dto.ProductoResponse;
import cl.duoc.productos.service.ProductoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de productos de la tienda.
 * - Consultar (GET): público.
 * - Crear, modificar, ajustar stock y activar/desactivar: ADMIN o VENDEDOR.
 * - Eliminar: solo ADMIN.
 * Las operaciones de gestión requieren el header X-Usuario-Id.
 */
@RestController
@RequestMapping("/productos")
public class ProductoController {

    private static final String HEADER_USUARIO = "X-Usuario-Id";

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /productos                         -> todos
    // GET /productos?categoriaId=1           -> de una categoría
    // GET /productos?activo=true             -> solo los activos
    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(required = false) Long categoriaId,
                                         @RequestParam(required = false) Boolean activo) {
        return productoService.listar(categoriaId, activo);
    }

    // GET /productos/{id}
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    // GET /productos/buscar?q=texto -> busca en SKU, nombre y descripción
    @GetMapping("/buscar")
    public List<ProductoResponse> buscar(@RequestParam("q") String texto) {
        return productoService.buscar(texto);
    }

    // GET /productos/precio?min=10000&max=300000 -> rango de precios
    @GetMapping("/precio")
    public List<ProductoResponse> porPrecio(@RequestParam Integer min, @RequestParam Integer max) {
        return productoService.porRangoDePrecio(min, max);
    }

    // GET /productos/stock-bajo?umbral=5 -> productos por reponer
    @GetMapping("/stock-bajo")
    public List<ProductoResponse> stockBajo(@RequestParam(defaultValue = "5") Integer umbral) {
        return productoService.stockBajo(umbral);
    }

    // POST /productos (ADMIN o VENDEDOR)
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@RequestHeader(HEADER_USUARIO) Long usuarioId,
                                                  @Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request, usuarioId);
        return ResponseEntity.created(URI.create("/productos/" + creado.id())).body(creado);
    }

    // PUT /productos/{id} (ADMIN o VENDEDOR)
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@RequestHeader(HEADER_USUARIO) Long usuarioId, @PathVariable Long id,
                                       @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request, usuarioId);
    }

    // PATCH /productos/{id}/stock?cantidad=-2 -> suma o descuenta unidades (ADMIN o VENDEDOR)
    @PatchMapping("/{id}/stock")
    public ProductoResponse ajustarStock(@RequestHeader(HEADER_USUARIO) Long usuarioId, @PathVariable Long id,
                                         @RequestParam Integer cantidad) {
        return productoService.ajustarStock(id, cantidad, usuarioId);
    }

    // PATCH /productos/{id}/estado?activo=false -> publica o retira el producto (ADMIN o VENDEDOR)
    @PatchMapping("/{id}/estado")
    public ProductoResponse cambiarEstado(@RequestHeader(HEADER_USUARIO) Long usuarioId, @PathVariable Long id,
                                          @RequestParam boolean activo) {
        return productoService.cambiarEstado(id, activo, usuarioId);
    }

    // DELETE /productos/{id} (solo ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(HEADER_USUARIO) Long usuarioId, @PathVariable Long id) {
        productoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
