package cl.duoc.productos.dto;

/**
 * Resumen de inventario por categoría (resultado de una consulta JPQL agrupada).
 */
public record ResumenCategoriaResponse(
        Long categoriaId,
        String categoria,
        Long cantidadProductos,
        Long unidadesEnStock,
        Long valorInventario) {
}
