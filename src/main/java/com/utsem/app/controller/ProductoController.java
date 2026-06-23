package com.utsem.app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

import com.utsem.app.dto.ProductoDTO;
import com.utsem.app.enums.Estatus;
import com.utsem.app.enums.Condicion;
import com.utsem.app.service.ProductoService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("rutaProductos")
public class ProductoController {

	@Autowired
	private ProductoService productoService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("productos", productoService.listar());
		return "carpetaProductos/paginaProductos";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("producto", new ProductoDTO());
		model.addAttribute("estados", Estatus.values());
		model.addAttribute("condiciones", Condicion.values());
		return "carpetaProductos/paginaFormulario";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("producto") ProductoDTO proDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("estados", Estatus.values());
			model.addAttribute("condiciones", Condicion.values());
			return "carpetaProductos/paginaFormulario";
		}
		productoService.guardar(proDto);
		return "redirect:/rutaProductos/listar";
	}

	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("producto") ProductoDTO proDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("estados", Estatus.values());
			model.addAttribute("condiciones", Condicion.values());
			return "carpetaProductos/paginaFormulario";
		}
		productoService.actualiza(proDto);
		return "redirect:/rutaProductos/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable UUID uuid) {
		model.addAttribute("producto", productoService.obtenerProductoUUID(uuid));
		model.addAttribute("estados", Estatus.values());
		model.addAttribute("condiciones", Condicion.values());
		return "carpetaProductos/paginaFormulario";
	}

	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable UUID uuid) {
		productoService.borrar2(uuid);
		return "redirect:/rutaProductos/listar";
	}
}