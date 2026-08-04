package com.utsem.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detProd", uniqueConstraints = {
	@jakarta.persistence.UniqueConstraint(columnNames = {"productId", "colorId", "transmision"})
})
public class DetProd {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private java.util.UUID uuid;

	@jakarta.persistence.PrePersist
	private void inicializarUuid() {
		this.uuid = java.util.UUID.randomUUID();
	}

	@ManyToOne
	@JoinColumn(name = "productId", nullable = false)
	private Producto producto;

	@ManyToOne
	@JoinColumn(name = "colorId")
	private Color color;

	@Column
	private Integer stock;

	@Column(length = 50)
	private com.utsem.app.enums.Transmision transmision;
}