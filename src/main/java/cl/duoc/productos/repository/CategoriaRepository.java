package cl.duoc.productos.repository;

import cl.duoc.productos.dto.ResumenCategoriaResponse;
import cl.duoc.productos.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    // Por cada categoría: cantidad de productos, unidades en stock y valor del inventario
    @Query("SELECT new cl.duoc.productos.dto.ResumenCategoriaResponse(c.id, c.nombre, COUNT(p), "
            + "COALESCE(SUM(p.stock), 0L), COALESCE(SUM(p.precio * p.stock), 0L)) "
            + "FROM Categoria c LEFT JOIN Producto p ON p.categoria = c "
            + "GROUP BY c.id, c.nombre ORDER BY c.nombre")
    List<ResumenCategoriaResponse> resumen();
}
