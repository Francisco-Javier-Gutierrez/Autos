package com.utsem.app.controller;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utsem.app.dto.PedidoDTO;
import com.utsem.app.enums.Estado;
import com.utsem.app.service.PedidoService;
import com.utsem.app.service.DetProdService;
import com.utsem.app.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("rutaPedidos")
public class PedidoController {

	@Autowired
	PedidoService pedidoService;

	@Autowired
	DetProdService detProdService;

	@Autowired
	ClienteService clienteService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("mensaje", "Listado de Pedidos");
		model.addAttribute("pedidos", pedidoService.listar());
		model.addAttribute("randomCol", java.util.concurrent.ThreadLocalRandom.current().nextInt(4));
		return "carpetaPedidos/paginaPedidos";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("pedido", new PedidoDTO());
		model.addAttribute("estados", Estado.values());
		model.addAttribute("detalles", detProdService.listarEntidades());
		model.addAttribute("clientes", clienteService.listarEntidades());
		return "carpetaPedidos/paginaFormulario";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("pedido") PedidoDTO pedDto, BindingResult result,
			Model model) {
		if (result.hasErrors()) {
			model.addAttribute("estados", Estado.values());
			model.addAttribute("detalles", detProdService.listarEntidades());
			model.addAttribute("clientes", clienteService.listarEntidades());
			return "carpetaPedidos/paginaFormulario";
		}
		pedidoService.guardar(pedDto);
		return "redirect:/rutaPedidos/listar";
	}

	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("pedido") PedidoDTO pedDto, BindingResult result,
			Model model) {
		if (result.hasErrors()) {
			model.addAttribute("estados", Estado.values());
			model.addAttribute("detalles", detProdService.listarEntidades());
			model.addAttribute("clientes", clienteService.listarEntidades());
			return "carpetaPedidos/paginaFormulario";
		}
		pedidoService.actualiza(pedDto);
		return "redirect:/rutaPedidos/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable UUID uuid) {
		model.addAttribute("pedido", pedidoService.obtenerPedidoUUID(uuid));
		model.addAttribute("estados", Estado.values());
		model.addAttribute("detalles", detProdService.listarEntidades());
		model.addAttribute("clientes", clienteService.listarEntidades());
		return "carpetaPedidos/paginaFormulario";
	}

	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable UUID uuid) {
		pedidoService.borrar(uuid);
		return "redirect:/rutaPedidos/listar";
	}
}