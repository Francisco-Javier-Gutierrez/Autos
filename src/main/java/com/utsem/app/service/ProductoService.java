package com.utsem.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.utsem.app.dto.ProductoDTO;
import com.utsem.app.enums.Estatus;
import com.utsem.app.model.Producto;
import com.utsem.app.repo.ProductoRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ProductoService {

	@Autowired
	private ProductoRepo productoRepo;

	@Autowired
	private ModelMapper mapper;

	public List<ProductoDTO> listar() {
		return productoRepo.findAll().stream()
				.map(p -> mapper.map(p, ProductoDTO.class))
				.toList();
	}

	public void guardar(ProductoDTO proDto) {
		Producto producto = mapper.map(proDto, Producto.class);
		if (producto.getUuid() == null) {
			producto.setUuid(UUID.randomUUID());
		}
		productoRepo.save(producto);
	}

	public void actualiza(ProductoDTO proDto) {
		Optional<Producto> opt = productoRepo.findByUuid(proDto.getUuid());
		if (opt.isPresent()) {
			Producto existente = opt.get(); 
			mapper.map(proDto, existente);
			productoRepo.save(existente);
		} else {
			throw new EntityNotFoundException("No se encontró el auto con UUID: " + proDto.getUuid());
		}
	}

	public ProductoDTO obtenerProductoUUID(UUID uuid) {
		Producto p = productoRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("No se encontró el coche con UUID: " + uuid));
		return mapper.map(p, ProductoDTO.class);
	}

	@Transactional
	public void borrar2(UUID uuid) {
		Producto p = productoRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("No se encontró el coche"));
		p.setEstado(Estatus.Discontinuado); // Soft Delete
		productoRepo.save(p);
	}
	
	public List<Producto> listarEntidades() {
		return productoRepo.findAll();
	}
}