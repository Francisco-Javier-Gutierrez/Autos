package com.utsem.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.utsem.app.enums.Estado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	private Integer version;

	@Column
	private Integer idDetProd;

	@Column
	private Integer clienteId;

	@Column(length = 100)
	private String numFactura;

	@Column
	private LocalDateTime fechaPedido;

	@Column
	private Integer cantidad;

	@Column(precision = 12, scale = 2)
	private BigDecimal total;

	@Column
	private Estado estadoPedido;

	@Column
	private Integer valoracionRubiFA;

	@Column
	private Boolean favoritoPerlaMM;

	@Column
	private String resenaFrancelyAnaidGH;

	@Column
	private Integer nivelInteresFranciscoJavierGH;

	@Column(unique = true)
	private UUID uuid;

	@PrePersist
    private void inicializarUuid() {
        this.uuid = UUID.randomUUID();
    }
}