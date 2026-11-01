package cl.duoc.productos.repository;

import cl.duoc.productos.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Productos de una categoría
    List<Producto> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

    // Productos de una categoría filtrados por estado
    List<Producto> findByCategoriaIdAndActivoOrderByNombreAsc(Long categoriaId, Boolean activo);

    // Productos activos o inactivos
    List<Producto> findByActivoOrderByNombreAsc(Boolean activo);

    // Búsqueda por texto en SKU, nombre o descripción
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) "
            + "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')) "
            + "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :texto, '%')) ORDER BY p.nombre")
    List<Producto> buscarPorTexto(@Param("texto") String texto);

    // Productos en un rango de precios, del más barato al más caro
    List<Producto> findByPrecioBetweenOrderByPrecioAsc(Integer min, Integer max);

    // Productos con stock igual o menor al umbral (para reponer)
    List<Producto> findByStockLessThanEqualOrderByStockAsc(Integer umbral);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    boolean existsByCategoriaId(Long categoriaId);
}
