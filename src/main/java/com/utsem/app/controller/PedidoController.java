package com.utsem.app.controller;

import java.util.UUID;
import java.util.List;
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
import com.utsem.app.model.DetProd;
import com.utsem.app.model.Cliente;
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

	private List<DetProd> obtenerDetallesFiltrados(PedidoDTO pedido) {
		return detProdService.listarEntidades().stream()
				.filter(d -> (d.getStock() != null && d.getStock() > 0) || (pedido != null && pedido.getDetProdUuid() != null && d.getUuid().equals(pedido.getDetProdUuid())))
				.toList();
	}

	private List<Cliente> obtenerClientesFiltrados(PedidoDTO pedido) {
		return clienteService.listarEntidades().stream()
				.filter(c -> c.getEstatus() == com.utsem.app.enums.EstatusCliente.Activo || (pedido != null && pedido.getClienteUuid() != null && c.getUuid().equals(pedido.getClienteUuid())))
				.toList();
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		PedidoDTO pedido = new PedidoDTO();
		model.addAttribute("pedido", pedido);
		model.addAttribute("estados", Estado.values());
		model.addAttribute("detalles", obtenerDetallesFiltrados(pedido));
		model.addAttribute("clientes", obtenerClientesFiltrados(pedido));
		return "carpetaPedidos/paginaFormulario";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("pedido") PedidoDTO pedDto, BindingResult result,
			Model model) {
		// Validar stock antes de guardar
		if (pedDto.getDetProdUuid() != null) {
			DetProd det = detProdService.listarEntidades().stream()
					.filter(d -> d.getUuid().equals(pedDto.getDetProdUuid()))
					.findFirst().orElse(null);
			if (det != null && pedDto.getCantidad() != null && pedDto.getCantidad() > det.getStock()) {
				result.rejectValue("cantidad", "error.cantidad", "¡No puedes comprar más productos de los que hay en stock (" + det.getStock() + " disponibles)!");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("estados", Estado.values());
			model.addAttribute("detalles", obtenerDetallesFiltrados(pedDto));
			model.addAttribute("clientes", obtenerClientesFiltrados(pedDto));
			return "carpetaPedidos/paginaFormulario";
		}
		
		try {
			pedidoService.guardar(pedDto);
		} catch (IllegalArgumentException ex) {
			result.rejectValue("cantidad", "error.cantidad", ex.getMessage());
			model.addAttribute("estados", Estado.values());
			model.addAttribute("detalles", obtenerDetallesFiltrados(pedDto));
			model.addAttribute("clientes", obtenerClientesFiltrados(pedDto));
			return "carpetaPedidos/paginaFormulario";
		}
		return "redirect:/rutaPedidos/listar";
	}

	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("pedido") PedidoDTO pedDto, BindingResult result,
			Model model) {
		// Validar stock antes de actualizar
		if (pedDto.getDetProdUuid() != null && pedDto.getUuid() != null) {
			try {
				PedidoDTO pedExistente = pedidoService.obtenerPedidoUUID(pedDto.getUuid());
				DetProd detNew = detProdService.listarEntidades().stream()
						.filter(d -> d.getUuid().equals(pedDto.getDetProdUuid()))
						.findFirst().orElse(null);
				if (detNew != null && pedDto.getCantidad() != null) {
					int diff = pedDto.getCantidad();
					if (pedExistente.getDetProdUuid() != null && pedExistente.getDetProdUuid().equals(pedDto.getDetProdUuid())) {
						diff = pedDto.getCantidad() - (pedExistente.getCantidad() != null ? pedExistente.getCantidad() : 0);
					}
					if (diff > detNew.getStock()) {
						result.rejectValue("cantidad", "error.cantidad", "¡No puedes comprar más productos de los que hay en stock (" + detNew.getStock() + " disponibles)!");
					}
				}
			} catch (Exception ex) {
				// ignorar pre-validación si falla la carga del existente
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("estados", Estado.values());
			model.addAttribute("detalles", obtenerDetallesFiltrados(pedDto));
			model.addAttribute("clientes", obtenerClientesFiltrados(pedDto));
			return "carpetaPedidos/paginaFormulario";
		}
		
		try {
			pedidoService.actualiza(pedDto);
		} catch (IllegalArgumentException ex) {
			result.rejectValue("cantidad", "error.cantidad", ex.getMessage());
			model.addAttribute("estados", Estado.values());
			model.addAttribute("detalles", obtenerDetallesFiltrados(pedDto));
			model.addAttribute("clientes", obtenerClientesFiltrados(pedDto));
			return "carpetaPedidos/paginaFormulario";
		}
		return "redirect:/rutaPedidos/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable UUID uuid) {
		PedidoDTO pedido = pedidoService.obtenerPedidoUUID(uuid);
		model.addAttribute("pedido", pedido);
		model.addAttribute("estados", Estado.values());
		model.addAttribute("detalles", obtenerDetallesFiltrados(pedido));
		model.addAttribute("clientes", obtenerClientesFiltrados(pedido));
		return "carpetaPedidos/paginaFormulario";
	}

	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable UUID uuid) {
		pedidoService.borrar(uuid);
		return "redirect:/rutaPedidos/listar";
	}
}