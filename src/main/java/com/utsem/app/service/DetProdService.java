package com.utsem.app.service;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.utsem.app.dto.DetProdDTO;
import com.utsem.app.model.DetProd;
import com.utsem.app.model.Producto;
import com.utsem.app.model.Color;
import com.utsem.app.repo.DetProdRepo;
import com.utsem.app.repo.ProductoRepo;
import com.utsem.app.repo.ColorRepo;
import jakarta.persistence.EntityNotFoundException;

@Service
public class DetProdService {

	@Autowired
	private DetProdRepo detProdRepo;

	@Autowired
	private ProductoRepo productoRepo;

	@Autowired
	private ColorRepo colorRepo;

	@Autowired
	private ModelMapper mapper;

	public List<DetProdDTO> listar() {
		return detProdRepo.findAll().stream()
				.map(det -> {
					DetProdDTO dto = mapper.map(det, DetProdDTO.class);
					if (det.getProducto() != null) {
						dto.setProductoId(det.getProducto().getId());
					}
					if (det.getColor() != null) {
						dto.setColorId(det.getColor().getId());
						dto.setColorNombre(det.getColor().getNombre());
					}
					return dto;
				}).toList();
	}

	public void guardar(DetProdDTO detProdDTO) {
		// Asignación manual segura para evitar el Error 500 de ModelMapper
		DetProd det = new DetProd();
		det.setStock(detProdDTO.getStock());
		det.setTransmision(detProdDTO.getTransmision());
		
		if (detProdDTO.getProductoId() != null) {
			Producto producto = productoRepo.findById(detProdDTO.getProductoId())
					.orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + detProdDTO.getProductoId()));
			det.setProducto(producto);
		}
		
		if (detProdDTO.getColorId() != null) {
			Color color = colorRepo.findById(detProdDTO.getColorId())
					.orElseThrow(() -> new EntityNotFoundException("Color no encontrado con ID: " + detProdDTO.getColorId()));
			det.setColor(color);
		}
		
		detProdRepo.save(det);
	}

	public void actualiza(DetProdDTO detProdDTO) {
		DetProd existente = detProdRepo.findByUuid(detProdDTO.getUuid())
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con el UUID: " + detProdDTO.getUuid()));
		
		existente.setStock(detProdDTO.getStock());
		existente.setTransmision(detProdDTO.getTransmision());
		
		if (detProdDTO.getProductoId() != null) {
			Producto producto = productoRepo.findById(detProdDTO.getProductoId())
					.orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + detProdDTO.getProductoId()));
			existente.setProducto(producto);
		} else {
			existente.setProducto(null);
		}
		
		if (detProdDTO.getColorId() != null) {
			Color color = colorRepo.findById(detProdDTO.getColorId())
					.orElseThrow(() -> new EntityNotFoundException("Color no encontrado con ID: " + detProdDTO.getColorId()));
			existente.setColor(color);
		} else {
			existente.setColor(null);
		}
		
		detProdRepo.save(existente);
	}

	public DetProdDTO obtenerPorUuid(java.util.UUID uuid) {
		DetProd det = detProdRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));
		DetProdDTO dto = mapper.map(det, DetProdDTO.class);
		if (det.getProducto() != null) {
			dto.setProductoId(det.getProducto().getId());
		}
		if (det.getColor() != null) {
			dto.setColorId(det.getColor().getId());
			dto.setColorNombre(det.getColor().getNombre());
		}
		return dto;
	}

	public void borrar(java.util.UUID uuid) {
		DetProd det = detProdRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));
		detProdRepo.delete(det);
	}

	public List<DetProd> listarEntidades() {
		return detProdRepo.findAll();
	}
}