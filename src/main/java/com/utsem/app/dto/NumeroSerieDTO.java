package com.utsem.app.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.utsem.app.enums.EstadoUnidad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumeroSerieDTO {

	private UUID uuid;

	@NotNull(message = "¡Debes seleccionar un detalle de vehículo!")
	private UUID detProdUuid;

	private String detProdDescripcion;

	@NotBlank(message = "¡El número de serie es obligatorio!")
	private String numeroSerie;

	@NotNull(message = "¡El estado es obligatorio!")
	private EstadoUnidad estadoUnidad;

	private LocalDate fechaIngreso;

	private String ubicacion;

	private String observaciones;

	private UUID pedidoUuid;
	private String pedidoFactura;
}
