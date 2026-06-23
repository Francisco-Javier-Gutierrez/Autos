package com.utsem.app.model;

import java.util.UUID;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import com.utsem.app.enums.Estatus;
import com.utsem.app.enums.Condicion;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private UUID uuid;

	@Column(length = 50)
	private String marca;

	@Column(name = "subMarca", length = 50)
	private String subMarca;

	@Column(length = 100)
	private String modelo;

	@Column
	private Double precio;

	@Column(name = "anio")
	private Integer anio;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private Estatus estado;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private Condicion condicion;

	@Column(name = "valoracionRubiFA")
	private Integer valoracionRubiFA;

	@Column(name = "favoritoPerlaMM")
	private Boolean favoritoPerlaMM;

	@Column(name = "resenaFrancelyAnaidGH", columnDefinition = "TEXT")
	private String resenaFrancelyAnaidGH;

	@Column(name = "nivelInteresFranciscoJavierGH")
	private Integer nivelInteresFranciscoJavierGH;

	@OneToMany(mappedBy = "producto")
	private List<DetProd> detalles;

	@PrePersist
	private void inicializarUuid() {
		this.uuid = UUID.randomUUID();
	}
}