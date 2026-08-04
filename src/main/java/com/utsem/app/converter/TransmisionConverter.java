package com.utsem.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.utsem.app.enums.Transmision;

@Converter(autoApply = true)
public class TransmisionConverter implements AttributeConverter<Transmision, String> {

	@Override
	public String convertToDatabaseColumn(Transmision attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.name();
	}

	@Override
	public Transmision convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		dbData = dbData.trim();
		if (dbData.equalsIgnoreCase("Automática") || dbData.equalsIgnoreCase("Automatica")) {
			return Transmision.Automatica;
		}
		if (dbData.equalsIgnoreCase("Manual")) {
			return Transmision.Manual;
		}
		if (dbData.equalsIgnoreCase("CVT")) {
			return Transmision.CVT;
		}
		if (dbData.equalsIgnoreCase("Doble_Embrague") || dbData.equalsIgnoreCase("Doble Embrague")) {
			return Transmision.Doble_Embrague;
		}
		if (dbData.equalsIgnoreCase("Semiautomática") || dbData.equalsIgnoreCase("Semiautomatica")) {
			return Transmision.Semiautomatica;
		}
		try {
			return Transmision.valueOf(dbData);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
