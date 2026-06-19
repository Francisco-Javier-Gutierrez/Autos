package com.utsem.app.dto;

import java.util.UUID;
import com.utsem.app.enums.Estatus;
import com.utsem.app.enums.Condicion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

	private UUID uuid;

	@NotBlank(message = "¡La marca es obligatoria!")
	private String marca;

	@NotBlank(message = "¡La submarca es obligatoria!")
	private String subMarca;

	@NotBlank(message = "¡El modelo es obligatorio!")
	private String modelo;

	@NotNull(message = "¡El precio es obligatorio!")
	@Min(value = 1, message = "¡El producto debe valer por lo menos 1 peso!")
	private Double precio;

	@NotNull(message = "¡El año es obligatorio!")
	private Integer anio;

	@NotNull(message = "¡El estado es obligatorio!")
	private Estatus estado;

	@NotNull(message = "¡La condición es obligatoria!")
	private Condicion condicion;

	@NotNull(message = "¡La valoraciòn es obligatoria!")
	private Integer valoracionRubiFA;
	
	@NotNull(message = "¡Lo favorito es obligatorio!")
	private Boolean favoritoPerlaMM;
	
	@NotNull(message = "¡La reseña es obligatoria!")
	private String resenaFrancelyAnaidGH;
	
	@NotNull(message = "¡El  nivel de interes es obligatorio!")
	private Integer nivelInteresFranciscoJavierGH;
}