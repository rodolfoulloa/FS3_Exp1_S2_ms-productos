package cl.duoc.productos.service;

import cl.duoc.productos.dto.ProductoRequest;
import cl.duoc.productos.dto.ProductoResponse;
import java.util.List;

public interface ProductoService {

    List<ProductoResponse> listar(Long categoriaId, Boolean activo);

    ProductoResponse obtenerPorId(Long id);

    List<ProductoResponse> buscar(String texto);

    List<ProductoResponse> porRangoDePrecio(Integer min, Integer max);

    List<ProductoResponse> stockBajo(Integer umbral);

    ProductoResponse crear(ProductoRequest request, Long usuarioId);

    ProductoResponse actualizar(Long id, ProductoRequest request, Long usuarioId);

    ProductoResponse ajustarStock(Long id, Integer cantidad, Long usuarioId);

    ProductoResponse cambiarEstado(Long id, boolean activo, Long usuarioId);

    void eliminar(Long id, Long usuarioId);
}
