package com.utsem.app.service;

import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utsem.app.dto.PedidoDTO;
import com.utsem.app.model.Pedido;
import com.utsem.app.model.Cliente;
import com.utsem.app.model.DetProd;
import com.utsem.app.repo.PedidoRepo;
import com.utsem.app.repo.ClienteRepo;
import com.utsem.app.repo.DetProdRepo;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@Transactional
public class PedidoService {

	@Autowired
	PedidoRepo pedidoRepo;

	@Autowired
	DetProdRepo detProdRepo;

	@Autowired
	ClienteRepo clienteRepo;

	@Autowired
	ModelMapper mapper;

	public List<PedidoDTO> listar() {
		return pedidoRepo.findAll().stream()
				.map(pedido -> {
					PedidoDTO dto = mapper.map(pedido, PedidoDTO.class);
					if (pedido.getDetProd() != null) {
						dto.setIdDetProd(pedido.getDetProd().getId());
						StringBuilder sb = new StringBuilder();
						if (pedido.getDetProd().getProducto() != null) {
							sb.append(pedido.getDetProd().getProducto().getMarca()).append(" ")
							  .append(pedido.getDetProd().getProducto().getSubMarca()).append(" ")
							  .append(pedido.getDetProd().getProducto().getModelo()).append(" (")
							  .append(pedido.getDetProd().getProducto().getAnio()).append(")");
						} else {
							sb.append("Detalle ID: ").append(pedido.getDetProd().getId());
						}
						if (pedido.getDetProd().getColor() != null) {
							sb.append(" - ").append(pedido.getDetProd().getColor().getNombre());
						}
						if (pedido.getDetProd().getTransmision() != null) {
							sb.append(" [").append(pedido.getDetProd().getTransmision()).append("]");
						}
						dto.setDetProdInfo(sb.toString());
					}
					if (pedido.getCliente() != null) {
						dto.setClienteId(pedido.getCliente().getId());
						dto.setClienteNombre(pedido.getCliente().getNombre());
					}
					return dto;
				}).toList();
	}

	public void guardar(PedidoDTO pedidoDTO) {
		Pedido pedido = mapper.map(pedidoDTO, Pedido.class);
		pedido.setId(null);
		
		DetProd det = null;
		if (pedidoDTO.getIdDetProd() != null) {
			det = detProdRepo.findById(pedidoDTO.getIdDetProd())
					.orElseThrow(() -> new EntityNotFoundException("Detalle de producto no encontrado con ID: " + pedidoDTO.getIdDetProd()));
			pedido.setDetProd(det);
		}
		
		if (pedidoDTO.getClienteId() != null) {
			Cliente cli = clienteRepo.findById(pedidoDTO.getClienteId())
					.orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + pedidoDTO.getClienteId()));
			pedido.setCliente(cli);
		}
		
		// Generar número de factura automáticamente
		if (pedido.getNumFactura() == null || pedido.getNumFactura().isBlank()) {
			String autoFactura = "FAC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", new Random().nextInt(10000));
			pedido.setNumFactura(autoFactura);
		}
		
		// Calcular total automáticamente (cantidad * precio del producto)
		if (det != null && det.getProducto() != null && det.getProducto().getPrecio() != null && pedidoDTO.getCantidad() != null) {
			BigDecimal precio = BigDecimal.valueOf(det.getProducto().getPrecio());
			BigDecimal totalCalculado = precio.multiply(BigDecimal.valueOf(pedidoDTO.getCantidad()));
			pedido.setTotal(totalCalculado);
		} else {
			pedido.setTotal(BigDecimal.ZERO);
		}
		
		pedidoRepo.save(pedido);
	}

	public void actualiza(PedidoDTO pedidoDTO) {
		Pedido pedidoExistente = pedidoRepo.findByUuid(pedidoDTO.getUuid())
				.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con el UUID: " + pedidoDTO.getUuid()));
		
		DetProd det = null;
		if (pedidoDTO.getIdDetProd() != null) {
			det = detProdRepo.findById(pedidoDTO.getIdDetProd())
					.orElseThrow(() -> new EntityNotFoundException("Detalle de producto no encontrado con ID: " + pedidoDTO.getIdDetProd()));
			pedidoExistente.setDetProd(det);
		} else {
			pedidoExistente.setDetProd(null);
		}
		
		if (pedidoDTO.getClienteId() != null) {
			Cliente cli = clienteRepo.findById(pedidoDTO.getClienteId())
					.orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + pedidoDTO.getClienteId()));
			pedidoExistente.setCliente(cli);
		} else {
			pedidoExistente.setCliente(null);
		}
		
		// Calcular total automáticamente (cantidad * precio del producto)
		if (det != null && det.getProducto() != null && det.getProducto().getPrecio() != null && pedidoDTO.getCantidad() != null) {
			BigDecimal precio = BigDecimal.valueOf(det.getProducto().getPrecio());
			BigDecimal totalCalculado = precio.multiply(BigDecimal.valueOf(pedidoDTO.getCantidad()));
			pedidoExistente.setTotal(totalCalculado);
		} else {
			pedidoExistente.setTotal(BigDecimal.ZERO);
		}
		
		// Mantener número de factura existente o generar uno nuevo si está vacío
		if (pedidoExistente.getNumFactura() == null || pedidoExistente.getNumFactura().isBlank()) {
			if (pedidoDTO.getNumFactura() != null && !pedidoDTO.getNumFactura().isBlank()) {
				pedidoExistente.setNumFactura(pedidoDTO.getNumFactura());
			} else {
				String autoFactura = "FAC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", new Random().nextInt(10000));
				pedidoExistente.setNumFactura(autoFactura);
			}
		}
		
		pedidoExistente.setCantidad(pedidoDTO.getCantidad());
		pedidoExistente.setEstadoPedido(pedidoDTO.getEstadoPedido());
		pedidoExistente.setValoracionRubiFA(pedidoDTO.getValoracionRubiFA());
		pedidoExistente.setFavoritoPerlaMM(pedidoDTO.getFavoritoPerlaMM());
		pedidoExistente.setResenaFrancelyAnaidGH(pedidoDTO.getResenaFrancelyAnaidGH());
		pedidoExistente.setNivelInteresFranciscoJavierGH(pedidoDTO.getNivelInteresFranciscoJavierGH());

		pedidoRepo.saveAndFlush(pedidoExistente);
	}

	public void borrar(UUID uuid) {
		Pedido pedidoExistente = pedidoRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con el UUID: " + uuid));
		pedidoRepo.delete(pedidoExistente);
	}

	public PedidoDTO obtenerPedidoUUID(UUID uuid) {
		Pedido pedido = pedidoRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con el UUID: " + uuid));
		PedidoDTO dto = mapper.map(pedido, PedidoDTO.class);
		if (pedido.getDetProd() != null) {
			dto.setIdDetProd(pedido.getDetProd().getId());
			StringBuilder sb = new StringBuilder();
			if (pedido.getDetProd().getProducto() != null) {
				sb.append(pedido.getDetProd().getProducto().getMarca()).append(" ")
				  .append(pedido.getDetProd().getProducto().getSubMarca()).append(" ")
				  .append(pedido.getDetProd().getProducto().getModelo()).append(" (")
				  .append(pedido.getDetProd().getProducto().getAnio()).append(")");
			} else {
				sb.append("Detalle ID: ").append(pedido.getDetProd().getId());
			}
			if (pedido.getDetProd().getColor() != null) {
				sb.append(" - ").append(pedido.getDetProd().getColor().getNombre());
			}
			if (pedido.getDetProd().getTransmision() != null) {
				sb.append(" [").append(pedido.getDetProd().getTransmision()).append("]");
			}
			dto.setDetProdInfo(sb.toString());
		}
		if (pedido.getCliente() != null) {
			dto.setClienteId(pedido.getCliente().getId());
			dto.setClienteNombre(pedido.getCliente().getNombre());
		}
		return dto;
	}
}