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
	com.utsem.app.repo.ProductoRepo productoRepo;

	@Autowired
	ModelMapper mapper;

	public List<PedidoDTO> listar() {
		return pedidoRepo.findAll().stream()
				.map(pedido -> {
					PedidoDTO dto = mapper.map(pedido, PedidoDTO.class);
					if (pedido.getDetProd() != null) {
						dto.setDetProdUuid(pedido.getDetProd().getUuid());
						StringBuilder sb = new StringBuilder();
						if (pedido.getDetProd().getProducto() != null) {
							sb.append(pedido.getDetProd().getProducto().getMarca()).append(" ")
							  .append(pedido.getDetProd().getProducto().getSubMarca()).append(" ")
							  .append(pedido.getDetProd().getProducto().getModelo()).append(" (")
							  .append(pedido.getDetProd().getProducto().getAnio()).append(")");
						} else {
							sb.append("Detalle UUID: ").append(pedido.getDetProd().getUuid());
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
						dto.setClienteUuid(pedido.getCliente().getUuid());
						dto.setClienteNombre(pedido.getCliente().getNombre());
					}
					return dto;
				}).toList();
	}

	public void guardar(PedidoDTO pedidoDTO) {
		Pedido pedido = mapper.map(pedidoDTO, Pedido.class);
		pedido.setId(null);
		
		DetProd det = null;
		if (pedidoDTO.getDetProdUuid() != null) {
			det = detProdRepo.findByUuid(pedidoDTO.getDetProdUuid())
					.orElseThrow(() -> new EntityNotFoundException("Detalle de producto no encontrado con UUID: " + pedidoDTO.getDetProdUuid()));
			pedido.setDetProd(det);
		}
		
		if (pedidoDTO.getClienteUuid() != null) {
			Cliente cli = clienteRepo.findByUuid(pedidoDTO.getClienteUuid())
					.orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con UUID: " + pedidoDTO.getClienteUuid()));
			pedido.setCliente(cli);
		}
		
		// Generar número de factura automáticamente
		if (pedido.getNumFactura() == null || pedido.getNumFactura().isBlank()) {
			String autoFactura = "FAC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", new Random().nextInt(10000));
			pedido.setNumFactura(autoFactura);
		}
		
		// Validar y descontar stock
		if (det != null) {
			if (pedidoDTO.getCantidad() == null || pedidoDTO.getCantidad() < 1) {
				throw new IllegalArgumentException("La cantidad debe ser al menos 1");
			}
			if (pedidoDTO.getCantidad() > det.getStock()) {
				throw new IllegalArgumentException("No hay suficiente stock. Disponibles: " + det.getStock());
			}
			det.setStock(det.getStock() - pedidoDTO.getCantidad());
			detProdRepo.save(det);
			actualizarEstadoProducto(det);
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
		
		DetProd detOld = pedidoExistente.getDetProd();
		DetProd detNew = null;
		if (pedidoDTO.getDetProdUuid() != null) {
			detNew = detProdRepo.findByUuid(pedidoDTO.getDetProdUuid())
					.orElseThrow(() -> new EntityNotFoundException("Detalle de producto no encontrado con UUID: " + pedidoDTO.getDetProdUuid()));
			pedidoExistente.setDetProd(detNew);
		} else {
			pedidoExistente.setDetProd(null);
		}
		
		if (pedidoDTO.getClienteUuid() != null) {
			Cliente cli = clienteRepo.findByUuid(pedidoDTO.getClienteUuid())
					.orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con UUID: " + pedidoDTO.getClienteUuid()));
			pedidoExistente.setCliente(cli);
		} else {
			pedidoExistente.setCliente(null);
		}
		
		// Validar y ajustar stock
		if (detOld != detNew) {
			// Devolver stock al anterior
			if (detOld != null) {
				detOld.setStock(detOld.getStock() + (pedidoExistente.getCantidad() != null ? pedidoExistente.getCantidad() : 0));
				detProdRepo.save(detOld);
				actualizarEstadoProducto(detOld);
			}
			// Descontar stock del nuevo
			if (detNew != null) {
				if (pedidoDTO.getCantidad() == null || pedidoDTO.getCantidad() < 1) {
					throw new IllegalArgumentException("La cantidad debe ser al menos 1");
				}
				if (pedidoDTO.getCantidad() > detNew.getStock()) {
					throw new IllegalArgumentException("No hay suficiente stock. Disponibles: " + detNew.getStock());
				}
				detNew.setStock(detNew.getStock() - pedidoDTO.getCantidad());
				detProdRepo.save(detNew);
				actualizarEstadoProducto(detNew);
			}
		} else if (detNew != null) {
			// Mismo detProd, validar diferencia
			int oldCant = pedidoExistente.getCantidad() != null ? pedidoExistente.getCantidad() : 0;
			int newCant = pedidoDTO.getCantidad() != null ? pedidoDTO.getCantidad() : 0;
			int diff = newCant - oldCant;
			if (diff > detNew.getStock()) {
				throw new IllegalArgumentException("No hay suficiente stock. Disponibles: " + detNew.getStock());
			}
			detNew.setStock(detNew.getStock() - diff);
			detProdRepo.save(detNew);
			actualizarEstadoProducto(detNew);
		}
		
		// Calcular total automáticamente (cantidad * precio del producto)
		if (detNew != null && detNew.getProducto() != null && detNew.getProducto().getPrecio() != null && pedidoDTO.getCantidad() != null) {
			BigDecimal precio = BigDecimal.valueOf(detNew.getProducto().getPrecio());
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
		
		// Devolver stock al borrar el pedido
		if (pedidoExistente.getDetProd() != null) {
			DetProd det = pedidoExistente.getDetProd();
			det.setStock(det.getStock() + (pedidoExistente.getCantidad() != null ? pedidoExistente.getCantidad() : 0));
			detProdRepo.save(det);
			actualizarEstadoProducto(det);
		}
		
		pedidoRepo.delete(pedidoExistente);
	}

	public List<Pedido> listarEntidades() {
		return pedidoRepo.findAll();
	}

	public PedidoDTO obtenerPedidoUUID(UUID uuid) {
		Pedido pedido = pedidoRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con el UUID: " + uuid));
		PedidoDTO dto = mapper.map(pedido, PedidoDTO.class);
		if (pedido.getDetProd() != null) {
			dto.setDetProdUuid(pedido.getDetProd().getUuid());
			StringBuilder sb = new StringBuilder();
			if (pedido.getDetProd().getProducto() != null) {
				sb.append(pedido.getDetProd().getProducto().getMarca()).append(" ")
				  .append(pedido.getDetProd().getProducto().getSubMarca()).append(" ")
				  .append(pedido.getDetProd().getProducto().getModelo()).append(" (")
				  .append(pedido.getDetProd().getProducto().getAnio()).append(")");
			} else {
				sb.append("Detalle UUID: ").append(pedido.getDetProd().getUuid());
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
			dto.setClienteUuid(pedido.getCliente().getUuid());
			dto.setClienteNombre(pedido.getCliente().getNombre());
		}
		return dto;
	}

	private void actualizarEstadoProducto(DetProd det) {
		if (det != null && det.getProducto() != null) {
			com.utsem.app.model.Producto prod = det.getProducto();
			List<DetProd> detalles = detProdRepo.findByProductoId(prod.getId());
			int totalStock = detalles.stream()
					.mapToInt(d -> {
						if (d.getId() != null && d.getId().equals(det.getId())) {
							return det.getStock() != null ? det.getStock() : 0;
						}
						return d.getStock() != null ? d.getStock() : 0;
					})
					.sum();
			if (totalStock <= 0) {
				prod.setEstado(com.utsem.app.enums.Estatus.Agotado);
			} else if (prod.getEstado() == com.utsem.app.enums.Estatus.Agotado) {
				prod.setEstado(com.utsem.app.enums.Estatus.Disponible);
			}
			productoRepo.save(prod);
		}
	}
}