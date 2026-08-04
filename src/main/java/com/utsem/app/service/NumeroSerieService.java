package com.utsem.app.service;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utsem.app.dto.NumeroSerieDTO;
import com.utsem.app.enums.EstadoUnidad;
import com.utsem.app.model.Pedido;
import com.utsem.app.model.DetProd;
import com.utsem.app.model.NumeroSerie;
import com.utsem.app.repo.PedidoRepo;
import com.utsem.app.repo.DetProdRepo;
import com.utsem.app.repo.NumeroSerieRepo;

import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class NumeroSerieService {

	@Autowired
	private NumeroSerieRepo numeroSerieRepo;

	@Autowired
	private DetProdRepo detProdRepo;

	@Autowired
	private PedidoRepo pedidoRepo;

	@Autowired
	private ModelMapper mapper;

	public List<NumeroSerieDTO> listar() {
		return numeroSerieRepo.findAll().stream()
				.map(this::convertirADTO)
				.toList();
	}

	public List<NumeroSerieDTO> listarPorDetProd(UUID detProdUuid) {
		DetProd detProd = detProdRepo.findByUuid(detProdUuid)
				.orElseThrow(() -> new EntityNotFoundException("Detalle de producto no encontrado con UUID: " + detProdUuid));
		return numeroSerieRepo.findByDetProdId(detProd.getId()).stream()
				.map(this::convertirADTO)
				.toList();
	}

	public void guardar(NumeroSerieDTO dto) {
		NumeroSerie ns = new NumeroSerie();
		ns.setNumeroSerie(dto.getNumeroSerie());
		ns.setEstadoUnidad(dto.getEstadoUnidad());
		ns.setFechaIngreso(dto.getFechaIngreso());
		ns.setUbicacion(dto.getUbicacion());
		ns.setObservaciones(dto.getObservaciones());

		if (dto.getDetProdUuid() != null) {
			DetProd detProd = detProdRepo.findByUuid(dto.getDetProdUuid())
					.orElseThrow(() -> new EntityNotFoundException("Detalle de producto no encontrado con UUID: " + dto.getDetProdUuid()));
			ns.setDetProd(detProd);
		}

		if (dto.getPedidoUuid() != null) {
			Pedido pedido = pedidoRepo.findByUuid(dto.getPedidoUuid())
					.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con UUID: " + dto.getPedidoUuid()));
			ns.setPedido(pedido);
		}

		numeroSerieRepo.save(ns);
		sincronizarStock(ns.getDetProd());
	}

	public void actualiza(NumeroSerieDTO dto) {
		NumeroSerie existente = numeroSerieRepo.findByUuid(dto.getUuid())
				.orElseThrow(() -> new EntityNotFoundException("Número de serie no encontrado con UUID: " + dto.getUuid()));

		existente.setNumeroSerie(dto.getNumeroSerie());
		existente.setEstadoUnidad(dto.getEstadoUnidad());
		existente.setFechaIngreso(dto.getFechaIngreso());
		existente.setUbicacion(dto.getUbicacion());
		existente.setObservaciones(dto.getObservaciones());

		if (dto.getDetProdUuid() != null) {
			DetProd detProd = detProdRepo.findByUuid(dto.getDetProdUuid())
					.orElseThrow(() -> new EntityNotFoundException("Detalle de producto no encontrado con UUID: " + dto.getDetProdUuid()));
			existente.setDetProd(detProd);
		}

		if (dto.getPedidoUuid() != null) {
			Pedido pedido = pedidoRepo.findByUuid(dto.getPedidoUuid())
					.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con UUID: " + dto.getPedidoUuid()));
			existente.setPedido(pedido);
		} else {
			existente.setPedido(null);
		}

		numeroSerieRepo.save(existente);
		sincronizarStock(existente.getDetProd());
	}

	public NumeroSerieDTO obtenerPorUuid(UUID uuid) {
		NumeroSerie ns = numeroSerieRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Número de serie no encontrado con UUID: " + uuid));
		return convertirADTO(ns);
	}

	public void borrar(UUID uuid) {
		NumeroSerie ns = numeroSerieRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Número de serie no encontrado con UUID: " + uuid));
		DetProd detProd = ns.getDetProd();
		numeroSerieRepo.delete(ns);
		sincronizarStock(detProd);
	}

	private void sincronizarStock(DetProd detProd) {
		long disponibles = numeroSerieRepo.countByDetProdIdAndEstadoUnidad(detProd.getId(), EstadoUnidad.Disponible);
		detProd.setStock((int) disponibles);
		detProdRepo.save(detProd);
	}

	public boolean existeNumeroSerie(String numeroSerie) {
		return numeroSerieRepo.findByNumeroSerie(numeroSerie).isPresent();
	}

	public boolean existeNumeroSerieExcluyendo(String numeroSerie, UUID uuid) {
		return numeroSerieRepo.findByNumeroSerie(numeroSerie)
				.map(ns -> !ns.getUuid().equals(uuid))
				.orElse(false);
	}

	private NumeroSerieDTO convertirADTO(NumeroSerie ns) {
		NumeroSerieDTO dto = mapper.map(ns, NumeroSerieDTO.class);

		if (ns.getDetProd() != null) {
			dto.setDetProdUuid(ns.getDetProd().getUuid());
			DetProd det = ns.getDetProd();
			String descripcion = "";
			if (det.getProducto() != null) {
				descripcion = det.getProducto().getMarca() + " " + det.getProducto().getSubMarca() + " " + det.getProducto().getModelo();
			}
			if (det.getColor() != null) {
				descripcion += " — " + det.getColor().getNombre();
			}
			if (det.getTransmision() != null) {
				descripcion += " (" + det.getTransmision().getDisplayName() + ")";
			}
			dto.setDetProdDescripcion(descripcion);
		}

		if (ns.getPedido() != null) {
			dto.setPedidoUuid(ns.getPedido().getUuid());
			dto.setPedidoFactura(ns.getPedido().getNumFactura());
		}

		return dto;
	}
}
