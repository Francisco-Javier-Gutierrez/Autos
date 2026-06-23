package com.utsem.app.model;

import java.util.UUID;
import com.utsem.app.enums.EstadoColor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "colores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Color {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50, nullable = false)
	private String nombre;

	@Column
	private EstadoColor estadoColor;

	@Column(unique = true)
	private UUID uuid;
	
	@PrePersist
	private void inicializarUuid() {
		this.uuid = UUID.randomUUID();
	}
}