package com.utsem.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.utsem.app.enums.Estado;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

	@NotNull(message = "¡Este campo es obligatorio!")
	private Integer idDetProd;

	@NotNull(message = "¡Este campo es obligatorio!")
	private Integer clienteId;

	@NotBlank(message = "¡Este campo es obligatorio!")
	private String numFactura;

	private LocalDateTime fechaPedido;

	@NotNull(message = "¡Este campo es obligatorio!")
	@Min(value = 1, message = "¡La cantidad debe ser al menos 1!")
	private Integer cantidad;

	@NotNull(message = "¡Este campo es obligatorio!")
	@Min(value = 0, message = "¡El total no debe ser negativo!")
	private BigDecimal total;

	private Estado estadoPedido;

	@Min(value = 1, message = "¡La valoración debe ser al menos 1!")
	@Max(value = 5, message = "¡La valoración máxima es 5!")
	private Integer valoracionRubiFA;

	private Boolean favoritoPerlaMM;

	private String resenaFrancelyAnaidGH;

	@Min(value = 1, message = "¡El nivel de interés debe ser al menos 1!")
	@Max(value = 3, message = "¡El nivel de interés máximo es 3!")
	private Integer nivelInteresFranciscoJavierGH;

	private UUID uuid;
}