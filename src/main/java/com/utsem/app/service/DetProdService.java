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
						dto.setProductoUuid(det.getProducto().getUuid());
						dto.setProductoNombre(det.getProducto().getMarca() + " " + det.getProducto().getSubMarca() + " " + det.getProducto().getModelo());
					}
					if (det.getColor() != null) {
						dto.setColorUuid(det.getColor().getUuid());
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
		
		if (detProdDTO.getProductoUuid() != null) {
			Producto producto = productoRepo.findByUuid(detProdDTO.getProductoUuid())
					.orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con UUID: " + detProdDTO.getProductoUuid()));
			det.setProducto(producto);
		}
		
		if (detProdDTO.getColorUuid() != null) {
			Color color = colorRepo.findByUuid(detProdDTO.getColorUuid())
					.orElseThrow(() -> new EntityNotFoundException("Color no encontrado con UUID: " + detProdDTO.getColorUuid()));
			det.setColor(color);
		}
		
		detProdRepo.save(det);
	}

	public void actualiza(DetProdDTO detProdDTO) {
		DetProd existente = detProdRepo.findByUuid(detProdDTO.getUuid())
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con el UUID: " + detProdDTO.getUuid()));
		
		existente.setStock(detProdDTO.getStock());
		existente.setTransmision(detProdDTO.getTransmision());
		
		if (detProdDTO.getProductoUuid() != null) {
			Producto producto = productoRepo.findByUuid(detProdDTO.getProductoUuid())
					.orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con UUID: " + detProdDTO.getProductoUuid()));
			existente.setProducto(producto);
		} else {
			existente.setProducto(null);
		}
		
		if (detProdDTO.getColorUuid() != null) {
			Color color = colorRepo.findByUuid(detProdDTO.getColorUuid())
					.orElseThrow(() -> new EntityNotFoundException("Color no encontrado con UUID: " + detProdDTO.getColorUuid()));
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
			dto.setProductoUuid(det.getProducto().getUuid());
			dto.setProductoNombre(det.getProducto().getMarca() + " " + det.getProducto().getSubMarca() + " " + det.getProducto().getModelo());
		}
		if (det.getColor() != null) {
			dto.setColorUuid(det.getColor().getUuid());
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