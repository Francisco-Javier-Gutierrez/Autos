package com.utsem.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utsem.app.dto.DetProdDTO;
import com.utsem.app.service.DetProdService;
import com.utsem.app.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("rutaDetalles")
public class DetProdController {

	@Autowired
	private DetProdService detProdService;

	@Autowired
	private ProductoService productoService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("detalles", detProdService.listar());
		return "carpetaDetalles/paginaDetalles";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("detalle", new DetProdDTO());
		model.addAttribute("productos", productoService.listarEntidades()); // Envía entidades completas
		return "carpetaDetalles/paginaFormularioDetalle";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("detalle") DetProdDTO detDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("productos", productoService.listarEntidades());
			return "carpetaDetalles/paginaFormularioDetalle";
		}
		detProdService.guardar(detDto);
		return "redirect:/rutaDetalles/listar";
	}

	@GetMapping("editar/{id}")
	public String metodoEditar(Model model, @PathVariable Long id) {
		model.addAttribute("detalle", detProdService.obtenerPorId(id));
		model.addAttribute("productos", productoService.listarEntidades());
		return "carpetaDetalles/paginaFormularioDetalle";
	}

	@GetMapping("eliminar/{id}")
	public String metodoElimina(@PathVariable Long id) {
		detProdService.borrar(id);
		return "redirect:/rutaDetalles/listar";
	}
}