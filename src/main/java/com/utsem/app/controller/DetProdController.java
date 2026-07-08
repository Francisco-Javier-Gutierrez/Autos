package com.utsem.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utsem.app.dto.DetProdDTO;
import com.utsem.app.service.DetProdService;
import com.utsem.app.service.ProductoService;
import com.utsem.app.service.ColorService;
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

	@Autowired
	private ColorService colorService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("detalles", detProdService.listar());
		return "carpetaDetalles/paginaDetalles";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("detalle", new DetProdDTO());
		model.addAttribute("productos", productoService.listarEntidades()); // Envía entidades completas
		model.addAttribute("colores", colorService.listar());
		return "carpetaDetalles/paginaFormularioDetalle";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("detalle") DetProdDTO detDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("productos", productoService.listarEntidades());
			model.addAttribute("colores", colorService.listar());
			return "carpetaDetalles/paginaFormularioDetalle";
		}
		detProdService.guardar(detDto);
		return "redirect:/rutaDetalles/listar";
	}

	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("detalle") DetProdDTO detDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("productos", productoService.listarEntidades());
			model.addAttribute("colores", colorService.listar());
			return "carpetaDetalles/paginaFormularioDetalle";
		}
		detProdService.actualiza(detDto);
		return "redirect:/rutaDetalles/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable java.util.UUID uuid) {
		model.addAttribute("detalle", detProdService.obtenerPorUuid(uuid));
		model.addAttribute("productos", productoService.listarEntidades());
		model.addAttribute("colores", colorService.listar());
		return "carpetaDetalles/paginaFormularioDetalle";
	}

	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable java.util.UUID uuid) {
		detProdService.borrar(uuid);
		return "redirect:/rutaDetalles/listar";
	}
}