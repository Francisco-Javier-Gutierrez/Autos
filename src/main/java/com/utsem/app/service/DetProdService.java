package com.utsem.app.service;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.utsem.app.dto.DetProdDTO;
import com.utsem.app.model.DetProd;
import com.utsem.app.model.Producto;
import com.utsem.app.repo.DetProdRepo;
import com.utsem.app.repo.ProductoRepo;
import jakarta.persistence.EntityNotFoundException;

@Service
public class DetProdService {

	@Autowired
	private DetProdRepo detProdRepo;

	@Autowired
	private ProductoRepo productoRepo;

	@Autowired
	private ModelMapper mapper;

	public List<DetProdDTO> listar() {
		return detProdRepo.findAll().stream()
				.map(det -> {
					DetProdDTO dto = mapper.map(det, DetProdDTO.class);
					if (det.getProducto() != null) {
						dto.setProductoId(det.getProducto().getId());
					}
					return dto;
				}).toList();
	}

	public void guardar(DetProdDTO detProdDTO) {
		// Asignación manual segura para evitar el Error 500 de ModelMapper
		DetProd det = new DetProd();
		det.setId(detProdDTO.getId());
		det.setColorId(detProdDTO.getColorId());
		det.setStock(detProdDTO.getStock());
		det.setTransmision(detProdDTO.getTransmision());
		
		if (detProdDTO.getProductoId() != null) {
			Producto producto = productoRepo.findById(detProdDTO.getProductoId())
					.orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + detProdDTO.getProductoId()));
			det.setProducto(producto);
		}
		
		detProdRepo.save(det);
	}

	public DetProdDTO obtenerPorId(Long id) {
		DetProd det = detProdRepo.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));
		DetProdDTO dto = mapper.map(det, DetProdDTO.class);
		if (det.getProducto() != null) {
			dto.setProductoId(det.getProducto().getId());
		}
		return dto;
	}

	public void borrar(Long id) {
		detProdRepo.deleteById(id);
	}
}