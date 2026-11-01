package cl.duoc.productos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear (POST) o modificar (PUT) un producto.
 */
public record ProductoRequest(

        @NotBlank(message = "El SKU es obligatorio")
        @Size(max = 30, message = "El SKU no puede superar 30 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El SKU solo admite letras, números y guiones")
        String sku,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        @Max(value = 99999999, message = "El precio no puede superar 99.999.999")
        Integer precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "La categoría (categoriaId) es obligatoria")
        Long categoriaId,

        Boolean activo) {
}
