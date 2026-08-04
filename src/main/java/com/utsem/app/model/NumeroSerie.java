package com.utsem.app.model;

import java.time.LocalDate;
import java.util.UUID;

import com.utsem.app.enums.EstadoUnidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "numeros_serie")
public class NumeroSerie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private UUID uuid;

	@PrePersist
	private void inicializarUuid() {
		if (this.uuid == null) {
			this.uuid = UUID.randomUUID();
		}
	}

	@ManyToOne
	@JoinColumn(name = "det_prod_id", nullable = false)
	private DetProd detProd;

	@Column(unique = true, nullable = false, length = 50)
	private String numeroSerie;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private EstadoUnidad estadoUnidad;

	@Column
	private LocalDate fechaIngreso;

	@Column(length = 100)
	private String ubicacion;

	@Column(columnDefinition = "TEXT")
	private String observaciones;

	@ManyToOne
	@JoinColumn(name = "pedido_id", nullable = true)
	private Pedido pedido;
}
