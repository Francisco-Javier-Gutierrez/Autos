package com.utsem.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.utsem.app.dto.ClienteDTO;
import com.utsem.app.model.Cliente;
import com.utsem.app.repo.ClienteRepo;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepo clienteRepo;

	@Autowired
	private ModelMapper mapper;

	public List<ClienteDTO> listar() {
		return clienteRepo.findAll().stream()
				.map(c -> mapper.map(c, ClienteDTO.class))
				.toList();
	}

	public void guardar(ClienteDTO clienteDto) {
		Cliente cliente = mapper.map(clienteDto, Cliente.class);
		if (cliente.getUuid() == null) {
			cliente.setUuid(UUID.randomUUID());
		}
		clienteRepo.save(cliente);
	}

	public void actualiza(ClienteDTO clienteDto) {
		Optional<Cliente> opt = clienteRepo.findByUuid(clienteDto.getUuid());
		if (opt.isPresent()) {
			Cliente existente = opt.get(); 
			mapper.map(clienteDto, existente);
			clienteRepo.save(existente);
		} else {
			throw new EntityNotFoundException("No se encontró el cliente con UUID: " + clienteDto.getUuid());
		}
	}

	public ClienteDTO obtenerClienteUUID(UUID uuid) {
		Cliente c = clienteRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("No se encontró el cliente con UUID: " + uuid));
		return mapper.map(c, ClienteDTO.class);
	}

	public void borrar(UUID uuid) {
		Cliente c = clienteRepo.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("No se encontró el cliente"));
		clienteRepo.delete(c);
	}
	
	public List<Cliente> listarEntidades() {
		return clienteRepo.findAll();
	}
}
