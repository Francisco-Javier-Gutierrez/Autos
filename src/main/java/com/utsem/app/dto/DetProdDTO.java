package com.utsem.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetProdDTO {

	private UUID uuid;

	@NotNull(message = "¡Debes seleccionar un producto válido!")
	private UUID productoUuid;

	@NotNull(message = "¡El color es obligatorio!")
	private UUID colorUuid;

	private String productoNombre;

	private String colorNombre;

	@NotNull(message = "¡El stock es obligatorio!")
	@Min(value = 0, message = "¡El stock no puede ser un número negativo!")
	private Integer stock;

	@NotBlank(message = "¡La transmisión es obligatoria!")
	private String transmision;
}