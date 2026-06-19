package com.utsem.app.dto;

import java.util.UUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

	private UUID uuid;

	@NotBlank(message = "¡El nombre es obligatorio!")
	private String nombre;

	@NotBlank(message = "¡El correo es obligatorio!")
	@Email(message = "¡El correo debe ser válido!")
	private String correo;

	@NotBlank(message = "¡El teléfono es obligatorio!")
	private String telefono;
}
