package com.utsem.app.controller;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utsem.app.dto.ClienteDTO;
import com.utsem.app.service.ClienteService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("rutaClientes")
public class ClienteController {

	@Autowired
	private ClienteService clienteService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("clientes", clienteService.listar());
		return "carpetaClientes/paginaClientes";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("cliente", new ClienteDTO());
		return "carpetaClientes/paginaFormulario";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("cliente") ClienteDTO clienteDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "carpetaClientes/paginaFormulario";
		}
		clienteService.guardar(clienteDto);
		return "redirect:/rutaClientes/listar";
	}

	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("cliente") ClienteDTO clienteDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "carpetaClientes/paginaFormulario";
		}
		clienteService.actualiza(clienteDto);
		return "redirect:/rutaClientes/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable UUID uuid) {
		model.addAttribute("cliente", clienteService.obtenerClienteUUID(uuid));
		return "carpetaClientes/paginaFormulario";
	}

	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable UUID uuid) {
		clienteService.borrar(uuid);
		return "redirect:/rutaClientes/listar";
	}
}
