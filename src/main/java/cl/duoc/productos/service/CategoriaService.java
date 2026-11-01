package cl.duoc.productos.service;

import cl.duoc.productos.dto.ResumenCategoriaResponse;
import cl.duoc.productos.model.Categoria;
import java.util.List;

public interface CategoriaService {

    List<Categoria> listar();

    Categoria obtenerPorId(Long id);

    List<ResumenCategoriaResponse> resumen();

    Categoria crear(Categoria categoria, Long usuarioId);

    Categoria actualizar(Long id, Categoria categoria, Long usuarioId);

    void eliminar(Long id, Long usuarioId);
}
