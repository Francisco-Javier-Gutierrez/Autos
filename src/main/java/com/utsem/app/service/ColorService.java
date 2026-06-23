package com.utsem.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import com.utsem.app.dto.ColorDTO;
import com.utsem.app.enums.EstadoColor; 
import com.utsem.app.model.Color;
import com.utsem.app.repo.ColorRepo;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ColorService {
	@Autowired
	ColorRepo colorRepo;
	@Autowired
	ModelMapper mapper;

	public List<ColorDTO> listar() {
		return colorRepo.findAll().stream().map(color -> mapper.map(color, ColorDTO.class)).toList();
	}

	public void guardar(ColorDTO colorDTO) {
		colorRepo.save(mapper.map(colorDTO, Color.class));
	}

	public void actualiza(ColorDTO colorDTO) {
		Optional<Color> optColor = colorRepo.findByUuid(colorDTO.getUuid());
		if (optColor.isPresent()) {
			mapper.map(colorDTO, optColor.get());
			colorRepo.save(optColor.get());
		} else {
			throw new EntityNotFoundException("Color no encontrado con el UUID: " + colorDTO.getUuid());
		}
	}

	@Transactional
	public void borrar(UUID uuid) {
		Optional<Color> optColor = colorRepo.findByUuid(uuid);
		if (optColor.isPresent()) {
			Color color = optColor.get();
			
			
			color.setEstadoColor(EstadoColor.Descontinuado); 
			
			
			colorRepo.save(color);
		} else {
			throw new EntityNotFoundException("Color no encontrado con el UUID: " + uuid);
		}
	}

	public ColorDTO obtenerColorUUID(UUID uuid) {
		Optional<Color> optColor = colorRepo.findByUuid(uuid);
		if (optColor.isPresent()) {
			return mapper.map(optColor.get(), ColorDTO.class);
		} else {
			throw new EntityNotFoundException("Color no encontrado con el UUID: " + uuid);
		}
	}
}