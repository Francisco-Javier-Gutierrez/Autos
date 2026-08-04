package com.utsem.app.enums;

public enum Transmision {
	Manual("Manual"),
	Automatica("Automática"),
	CVT("CVT (Continuamente Variable)"),
	Doble_Embrague("Doble Embrague (DCT/DSG)"),
	Semiautomatica("Semiautomática");

	private final String displayName;

	Transmision(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
