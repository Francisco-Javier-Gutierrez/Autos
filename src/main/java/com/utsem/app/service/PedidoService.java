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
import com.utsem.app.repo.ProductoRepo;
import com.utsem.app.repo.ClienteRepo;
import com.utsem.app.repo.DetProdRepo;
import com.utsem.app.repo.NumeroSerieRepo;
import com.utsem.app.model.NumeroSerie;
import com.utsem.app.enums.EstadoUnidad;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@Transactional
public class PedidoService {

	@Autowired
	private NumeroSerieRepo numeroSerieRepo;

	@Autowired
	PedidoRepo pedidoRepo;

	@Autowired
	DetProdRepo detProdRepo;

	@Autowired
	ClienteRepo clienteRepo;

	@Autowired
	ProductoRepo productoRepo;

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
		
		if (pedido.getNumFactura() == null || pedido.getNumFactura().isBlank()) {
			String autoFactura = "FAC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", new Random().nextInt(10000));
			pedido.setNumFactura(autoFactura);
		}
		
		if (det != null) {
			if (pedidoDTO.getNumeroSerieUuids() != null && !pedidoDTO.getNumeroSerieUuids().isEmpty()) {
				int cant = pedidoDTO.getNumeroSerieUuids().size();
				pedido.setCantidad(cant);
				pedidoDTO.setCantidad(cant);
			} else {
				if (pedidoDTO.getCantidad() == null || pedidoDTO.getCantidad() < 1) {
					throw new IllegalArgumentException("La cantidad debe ser al menos 1");
				}
				if (pedidoDTO.getCantidad() > det.getStock()) {
					throw new IllegalArgumentException("No hay suficiente stock. Disponibles: " + det.getStock());
				}
			}
		}
		
		if (det != null && det.getProducto() != null && det.getProducto().getPrecio() != null && pedidoDTO.getCantidad() != null) {
			BigDecimal precio = BigDecimal.valueOf(det.getProducto().getPrecio());
			BigDecimal totalCalculado = precio.multiply(BigDecimal.valueOf(pedidoDTO.getCantidad()));
			pedido.setTotal(totalCalculado);
		} else {
			pedido.setTotal(BigDecimal.ZERO);
		}
		
		pedido = pedidoRepo.save(pedido);

		if (det != null) {
			if (pedidoDTO.getNumeroSerieUuids() != null && !pedidoDTO.getNumeroSerieUuids().isEmpty()) {
				for (UUID nsUuid : pedidoDTO.getNumeroSerieUuids()) {
					NumeroSerie ns = numeroSerieRepo.findByUuid(nsUuid)
							.orElseThrow(() -> new EntityNotFoundException("Número de serie no encontrado con UUID: " + nsUuid));
					if (ns.getEstadoUnidad() != EstadoUnidad.Disponible) {
						throw new IllegalArgumentException("El número de serie " + ns.getNumeroSerie() + " no está disponible");
					}
					ns.setEstadoUnidad(EstadoUnidad.Vendido);
					ns.setPedido(pedido);
					ns.setUbicacion("Entregado a cliente / Asignado a Pedido");
					numeroSerieRepo.save(ns);
				}
			} else {
				int cantidadAAsignar = pedidoDTO.getCantidad();
				List<NumeroSerie> disponibles = numeroSerieRepo.findByDetProdIdAndEstadoUnidadOrderByFechaIngresoAsc(det.getId(), EstadoUnidad.Disponible);
				if (disponibles.size() < cantidadAAsignar) {
					throw new IllegalArgumentException("No hay suficientes números de serie disponibles. Requeridos: " + cantidadAAsignar + ", Disponibles: " + disponibles.size());
				}
				for (int i = 0; i < cantidadAAsignar; i++) {
					NumeroSerie ns = disponibles.get(i);
					ns.setEstadoUnidad(EstadoUnidad.Vendido);
					ns.setPedido(pedido);
					ns.setUbicacion("Entregado a cliente / Asignado a Pedido");
					numeroSerieRepo.save(ns);
				}
			}
			sincronizarStock(det);
		}
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
		
		if (pedidoDTO.getNumeroSerieUuids() != null && !pedidoDTO.getNumeroSerieUuids().isEmpty()) {
			pedidoDTO.setCantidad(pedidoDTO.getNumeroSerieUuids().size());
		}
		int newCant = pedidoDTO.getCantidad() != null ? pedidoDTO.getCantidad() : 0;
		if (newCant < 1) {
			throw new IllegalArgumentException("La cantidad debe ser al menos 1");
		}
		
		List<NumeroSerie> seriesAsignadas = numeroSerieRepo.findByPedidoId(pedidoExistente.getId());
		
		if (pedidoDTO.getNumeroSerieUuids() != null && !pedidoDTO.getNumeroSerieUuids().isEmpty()) {
			List<UUID> newUuids = pedidoDTO.getNumeroSerieUuids();
			
			for (NumeroSerie ns : seriesAsignadas) {
				if (!newUuids.contains(ns.getUuid())) {
					ns.setEstadoUnidad(EstadoUnidad.Disponible);
					ns.setPedido(null);
					ns.setUbicacion("Inventario Principal");
					numeroSerieRepo.save(ns);
				}
			}
			
			for (UUID nsUuid : newUuids) {
				boolean yaAsignado = seriesAsignadas.stream().anyMatch(ns -> ns.getUuid().equals(nsUuid));
				if (!yaAsignado) {
					NumeroSerie nsNew = numeroSerieRepo.findByUuid(nsUuid)
							.orElseThrow(() -> new EntityNotFoundException("Número de serie no encontrado con UUID: " + nsUuid));
					if (nsNew.getEstadoUnidad() != EstadoUnidad.Disponible) {
						throw new IllegalArgumentException("El número de serie " + nsNew.getNumeroSerie() + " no está disponible");
					}
					nsNew.setEstadoUnidad(EstadoUnidad.Vendido);
					nsNew.setPedido(pedidoExistente);
					nsNew.setUbicacion("Entregado a cliente / Asignado a Pedido");
					numeroSerieRepo.save(nsNew);
				}
			}
		} else {
			if (detOld != detNew) {
				for (NumeroSerie ns : seriesAsignadas) {
					ns.setEstadoUnidad(EstadoUnidad.Disponible);
					ns.setPedido(null);
					ns.setUbicacion("Inventario Principal");
					numeroSerieRepo.save(ns);
				}
				seriesAsignadas.clear();
				
				if (detNew != null) {
					if (newCant > detNew.getStock()) {
						throw new IllegalArgumentException("No hay suficiente stock. Disponibles: " + detNew.getStock());
					}
					List<NumeroSerie> disponibles = numeroSerieRepo.findByDetProdIdAndEstadoUnidadOrderByFechaIngresoAsc(detNew.getId(), EstadoUnidad.Disponible);
					if (disponibles.size() < newCant) {
						throw new IllegalArgumentException("No hay suficientes números de serie disponibles. Requeridos: " + newCant + ", Disponibles: " + disponibles.size());
					}
					for (int i = 0; i < newCant; i++) {
						NumeroSerie ns = disponibles.get(i);
						ns.setEstadoUnidad(EstadoUnidad.Vendido);
						ns.setPedido(pedidoExistente);
						ns.setUbicacion("Entregado a cliente / Asignado a Pedido");
						numeroSerieRepo.save(ns);
					}
				}
			} else if (detNew != null) {
				int oldCant = seriesAsignadas.size();
				if (newCant > oldCant) {
					int diff = newCant - oldCant;
					if (diff > detNew.getStock()) {
						throw new IllegalArgumentException("No hay suficiente stock. Disponibles: " + detNew.getStock());
					}
					List<NumeroSerie> disponibles = numeroSerieRepo.findByDetProdIdAndEstadoUnidadOrderByFechaIngresoAsc(detNew.getId(), EstadoUnidad.Disponible);
					if (disponibles.size() < diff) {
						throw new IllegalArgumentException("No hay suficientes números de serie disponibles. Requeridos adicionales: " + diff + ", Disponibles: " + disponibles.size());
					}
					for (int i = 0; i < diff; i++) {
						NumeroSerie ns = disponibles.get(i);
						ns.setEstadoUnidad(EstadoUnidad.Vendido);
						ns.setPedido(pedidoExistente);
						ns.setUbicacion("Entregado a cliente / Asignado a Pedido");
						numeroSerieRepo.save(ns);
					}
				} else if (newCant < oldCant) {
					int diff = oldCant - newCant;
					for (int i = 0; i < diff; i++) {
						NumeroSerie ns = seriesAsignadas.get(oldCant - 1 - i);
						ns.setEstadoUnidad(EstadoUnidad.Disponible);
						ns.setPedido(null);
						ns.setUbicacion("Inventario Principal");
						numeroSerieRepo.save(ns);
					}
				}
			}
		}

		if (detNew != null && detNew.getProducto() != null && detNew.getProducto().getPrecio() != null && pedidoDTO.getCantidad() != null) {
			BigDecimal precio = BigDecimal.valueOf(detNew.getProducto().getPrecio());
			BigDecimal totalCalculado = precio.multiply(BigDecimal.valueOf(pedidoDTO.getCantidad()));
			pedidoExistente.setTotal(totalCalculado);
		} else {
			pedidoExistente.setTotal(BigDecimal.ZERO);
		}
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

		if (detOld != null) {
			sincronizarStock(detOld);
		}
		if (detNew != null && detNew != detOld) {
			sincronizarStock(detNew);
		}
	}

	public void borrar(UUID uuid) {
		Pedido pedidoExistente = pedidoRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con el UUID: " + uuid));
		
		DetProd det = pedidoExistente.getDetProd();
		
		List<NumeroSerie> asignadas = numeroSerieRepo.findByPedidoId(pedidoExistente.getId());
		for (NumeroSerie ns : asignadas) {
			ns.setEstadoUnidad(EstadoUnidad.Disponible);
			ns.setPedido(null);
			ns.setUbicacion("Inventario Principal");
			numeroSerieRepo.save(ns);
		}
		
		pedidoRepo.delete(pedidoExistente);
		
		if (det != null) {
			sincronizarStock(det);
		}
	}

	private void sincronizarStock(DetProd detProd) {
		if (detProd != null) {
			long disponibles = numeroSerieRepo.countByDetProdIdAndEstadoUnidad(detProd.getId(), EstadoUnidad.Disponible);
			detProd.setStock((int) disponibles);
			detProdRepo.save(detProd);
			actualizarEstadoProducto(detProd);
		}
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
			dto.setClienteCorreo(pedido.getCliente().getCorreo());
			dto.setClienteTelefono(pedido.getCliente().getTelefono());
		}
		if (pedido.getDetProd() != null && pedido.getDetProd().getProducto() != null) {
			dto.setPrecioUnitario(pedido.getDetProd().getProducto().getPrecio());
		}
		
		// Cargar lista de números de serie específicos asignados a esta orden
		List<NumeroSerie> series = numeroSerieRepo.findByPedidoId(pedido.getId());
		if (!series.isEmpty()) {
			dto.setNumeroSerieUuids(series.stream().map(NumeroSerie::getUuid).toList());
			dto.setNumeroSerieTexts(series.stream().map(NumeroSerie::getNumeroSerie).toList());
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