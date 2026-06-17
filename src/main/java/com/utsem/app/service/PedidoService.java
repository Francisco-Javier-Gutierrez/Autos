package com.utsem.app.service;

import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utsem.app.dto.PedidoDTO;
import com.utsem.app.model.Pedido;
import com.utsem.app.repo.PedidoRepo;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class PedidoService {

	@Autowired
	PedidoRepo pedidoRepo;

	@Autowired
	ModelMapper mapper;

	public List<PedidoDTO> listar() {
		return pedidoRepo.findAll().stream().map(pedido -> mapper.map(pedido, PedidoDTO.class)).toList();
	}

	public void guardar(PedidoDTO pedidoDTO) {
		Pedido pedido = mapper.map(pedidoDTO, Pedido.class);
		pedido.setId(null);
		pedidoRepo.save(pedido);
	}

	public void actualiza(PedidoDTO pedidoDTO) {
		Pedido pedidoExistente = pedidoRepo.findByUuid(pedidoDTO.getUuid())
				.orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con el UUID: " + pedidoDTO.getUuid()));
		
		pedidoExistente.setIdDetProd(pedidoDTO.getIdDetProd());
		pedidoExistente.setClienteId(pedidoDTO.getClienteId());
		pedidoExistente.setNumFactura(pedidoDTO.getNumFactura());
		pedidoExistente.setCantidad(pedidoDTO.getCantidad());
		pedidoExistente.setTotal(pedidoDTO.getTotal());
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
		return mapper.map(pedido, PedidoDTO.class);
	}
}