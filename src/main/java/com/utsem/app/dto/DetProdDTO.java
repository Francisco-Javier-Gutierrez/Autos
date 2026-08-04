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

	private Integer stock;

	@NotNull(message = "¡La transmisión es obligatoria!")
	private com.utsem.app.enums.Transmision transmision;

	private String numerosSerieNuevos;
}