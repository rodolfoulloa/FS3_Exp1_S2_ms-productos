package cl.duoc.productos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.productos.controller.ProductoController;
import cl.duoc.productos.dto.ProductoRequest;
import cl.duoc.productos.dto.ProductoResponse;
import cl.duoc.productos.exception.AccesoDenegadoException;
import cl.duoc.productos.exception.RecursoNoEncontradoException;
import cl.duoc.productos.exception.ReglaNegocioException;
import cl.duoc.productos.service.ProductoService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas del controlador de productos sin conectarse a Oracle ni a ms-usuarios.
 */
@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    private ProductoResponse mouse() {
        return new ProductoResponse(8L, "ACC-LOG-001", "Mouse Logitech MX Master 3S", "Inalámbrico",
                79990, 25, 5L, "Accesorios", true, LocalDateTime.now(), null);
    }

    private static final String JSON = """
            {"sku":"ACC-LOG-001","nombre":"Mouse Logitech MX Master 3S","descripcion":"Inalámbrico",
             "precio":79990,"stock":25,"categoriaId":5}
            """;

    @Test
    void listar_devuelveProductos() throws Exception {
        when(productoService.listar(null, null)).thenReturn(List.of(mouse()));
        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("ACC-LOG-001"))
                .andExpect(jsonPath("$[0].categoria").value("Accesorios"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(productoService.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));
        mockMvc.perform(get("/productos/99")).andExpect(status().isNotFound());
    }

    @Test
    void buscarPorPrecio_devuelve200() throws Exception {
        when(productoService.porRangoDePrecio(50000, 100000)).thenReturn(List.of(mouse()));
        mockMvc.perform(get("/productos/precio").param("min", "50000").param("max", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precio").value(79990));
    }

    @Test
    void crear_comoVendedor_devuelve201() throws Exception {
        when(productoService.crear(any(ProductoRequest.class), eq(2L))).thenReturn(mouse());
        mockMvc.perform(post("/productos").header("X-Usuario-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8));
    }

    @Test
    void crear_sinHeader_devuelve400() throws Exception {
        mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_precioNegativo_devuelve400() throws Exception {
        mockMvc.perform(post("/productos").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"X-1\",\"nombre\":\"X\",\"precio\":-5,\"stock\":1,\"categoriaId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.precio").exists());
    }

    @Test
    void crear_comoCliente_devuelve403() throws Exception {
        when(productoService.crear(any(ProductoRequest.class), eq(3L)))
                .thenThrow(new AccesoDenegadoException("sin permiso"));
        mockMvc.perform(post("/productos").header("X-Usuario-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizar_devuelve200() throws Exception {
        when(productoService.actualizar(eq(8L), any(ProductoRequest.class), eq(1L))).thenReturn(mouse());
        mockMvc.perform(put("/productos/8").header("X-Usuario-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isOk());
    }

    @Test
    void ajustarStock_insuficiente_devuelve400() throws Exception {
        when(productoService.ajustarStock(8L, -100, 2L)).thenThrow(new ReglaNegocioException("Stock insuficiente"));
        mockMvc.perform(patch("/productos/8/stock").header("X-Usuario-Id", 2).param("cantidad", "-100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminar_comoAdmin_devuelve204() throws Exception {
        mockMvc.perform(delete("/productos/8").header("X-Usuario-Id", 1)).andExpect(status().isNoContent());
    }

    @Test
    void eliminar_comoVendedor_devuelve403() throws Exception {
        doThrow(new AccesoDenegadoException("solo ADMIN")).when(productoService).eliminar(8L, 2L);
        mockMvc.perform(delete("/productos/8").header("X-Usuario-Id", 2)).andExpect(status().isForbidden());
    }
}
