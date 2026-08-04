package com.utsem.app.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Optional;

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
import com.utsem.app.repo.NumeroSerieRepo;
import com.utsem.app.enums.Transmision;
import com.utsem.app.enums.EstadoUnidad;
import com.utsem.app.model.NumeroSerie;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class DetProdService {

	@Autowired
	private DetProdRepo detProdRepo;

	@Autowired
	private ProductoRepo productoRepo;

	@Autowired
	private ColorRepo colorRepo;

	@Autowired
	private NumeroSerieRepo numeroSerieRepo;

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
		det.setStock(0); // Se calculará después
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
		
		det = detProdRepo.save(det);

		// Crear los números de serie
		if (detProdDTO.getNumerosSerieNuevos() != null && !detProdDTO.getNumerosSerieNuevos().trim().isEmpty()) {
			String[] serials = detProdDTO.getNumerosSerieNuevos().split("[,\\n]+");
			// Primero validar todos para evitar registros parciales
			List<String> duplicados = new ArrayList<>();
			for (String serial : serials) {
				String s = serial.trim();
				if (!s.isEmpty()) {
					if (numeroSerieRepo.findByNumeroSerie(s).isPresent()) {
						duplicados.add(s);
					}
				}
			}
			if (!duplicados.isEmpty()) {
				throw new IllegalArgumentException("Los siguientes números de serie ya están registrados: " + String.join(", ", duplicados));
			}
			// Luego guardar
			for (String serial : serials) {
				String s = serial.trim();
				if (!s.isEmpty()) {
					NumeroSerie ns = new NumeroSerie();
					ns.setUuid(UUID.randomUUID());
					ns.setDetProd(det);
					ns.setNumeroSerie(s);
					ns.setEstadoUnidad(EstadoUnidad.Disponible);
					ns.setFechaIngreso(LocalDate.now());
					ns.setUbicacion("Inventario Principal");
					numeroSerieRepo.save(ns);
				}
			}
			long disp = numeroSerieRepo.countByDetProdIdAndEstadoUnidad(det.getId(), EstadoUnidad.Disponible);
			det.setStock((int) disp);
			detProdRepo.save(det);
		}
	}

	public void actualiza(DetProdDTO detProdDTO) {
		DetProd existente = detProdRepo.findByUuid(detProdDTO.getUuid())
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con el UUID: " + detProdDTO.getUuid()));
		
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

	public DetProdDTO obtenerPorUuid(UUID uuid) {
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

	public void borrar(UUID uuid) {
		DetProd det = detProdRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));
		detProdRepo.delete(det);
	}

	public List<DetProd> listarEntidades() {
		return detProdRepo.findAll();
	}

	public Optional<DetProd> buscarDuplicado(UUID productoUuid, UUID colorUuid, Transmision transmision) {
		if (productoUuid == null || colorUuid == null || transmision == null) {
			return Optional.empty();
		}
		return detProdRepo.findAll().stream()
				.filter(d -> d.getProducto() != null && d.getProducto().getUuid().equals(productoUuid))
				.filter(d -> d.getColor() != null && d.getColor().getUuid().equals(colorUuid))
				.filter(d -> d.getTransmision() != null && d.getTransmision() == transmision)
				.findFirst();
	}

	public void agregarStockConSeries(UUID uuid, String seriesList) {
		DetProd existente = detProdRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con el UUID: " + uuid));
		
		if (seriesList != null && !seriesList.trim().isEmpty()) {
			String[] serials = seriesList.split("[,\\n]+");
			// Primero validar todos
			List<String> duplicados = new ArrayList<>();
			for (String serial : serials) {
				String s = serial.trim();
				if (!s.isEmpty()) {
					if (numeroSerieRepo.findByNumeroSerie(s).isPresent()) {
						duplicados.add(s);
					}
				}
			}
			if (!duplicados.isEmpty()) {
				throw new IllegalArgumentException("Los siguientes números de serie ya están registrados: " + String.join(", ", duplicados));
			}
			// Luego guardar
			for (String serial : serials) {
				String s = serial.trim();
				if (!s.isEmpty()) {
					NumeroSerie ns = new NumeroSerie();
					ns.setUuid(UUID.randomUUID());
					ns.setDetProd(existente);
					ns.setNumeroSerie(s);
					ns.setEstadoUnidad(EstadoUnidad.Disponible);
					ns.setFechaIngreso(LocalDate.now());
					ns.setUbicacion("Inventario Principal");
					numeroSerieRepo.save(ns);
				}
			}
			long disp = numeroSerieRepo.countByDetProdIdAndEstadoUnidad(existente.getId(), EstadoUnidad.Disponible);
			existente.setStock((int) disp);
			detProdRepo.save(existente);
		}
	}
}