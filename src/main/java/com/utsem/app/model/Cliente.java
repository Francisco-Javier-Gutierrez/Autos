package com.utsem.app.model;

import java.util.UUID;

import com.utsem.app.enums.EstatusCliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private UUID uuid;

	@Column(length = 100, nullable = false)
	private String nombre;

	@Column(length = 100, nullable = false)
	private String correo;

	@Column(length = 20, nullable = false)
	private String telefono;

	@Enumerated(jakarta.persistence.EnumType.STRING)
	@Column(length = 30, nullable = false)
	private EstatusCliente estatus = EstatusCliente.Activo;

	@PrePersist
	private void inicializarUuid() {
		this.uuid = UUID.randomUUID();
	}
}
