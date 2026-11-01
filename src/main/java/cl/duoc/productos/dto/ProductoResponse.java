package cl.duoc.productos.dto;

import cl.duoc.productos.model.Producto;
import java.time.LocalDateTime;

/**
 * Representación JSON de un producto con el nombre de su categoría.
 */
public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        Integer precio,
        Integer stock,
        Long categoriaId,
        String categoria,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getSku(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getStock(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getActivo(),
                p.getFechaCreacion(),
                p.getFechaActualizacion());
    }
}
