package com.utsem.app.enums;

public enum EstadoUnidad {
	Disponible("Disponible"),
	Vendido("Vendido"),
	Reservado("Reservado"),
	En_Mantenimiento("En Mantenimiento");

	private final String displayName;

	EstadoUnidad(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
