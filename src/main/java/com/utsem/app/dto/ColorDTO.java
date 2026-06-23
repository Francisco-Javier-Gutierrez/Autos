package com.utsem.app.dto;

import java.util.UUID;

import com.utsem.app.enums.EstadoColor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ColorDTO {
	
		private Long id;
	
	@NotBlank(message = "Este campo es obligatorio")
	@Size(max = 50, message = "El nombre del color no debe superar los 50 caracteres")
	private String nombre;

	@NotNull(message = "Este campo es obligatorio")
	private EstadoColor estadoColor;
	
	private UUID uuid;
}