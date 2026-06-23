package com.utsem.app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

import com.utsem.app.dto.ColorDTO;
import com.utsem.app.enums.EstadoColor;
import com.utsem.app.service.ColorService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("rutaColores")
public class ColorController {

	@Autowired
	private ColorService colorService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("mensaje", "Módulo de gestión de Colores");
		model.addAttribute("color", new ColorDTO()); 
		model.addAttribute("colores", colorService.listar());
		model.addAttribute("estados", EstadoColor.values());
		return "carpetaColores/paginaColores";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("color", new ColorDTO());
		model.addAttribute("estados", EstadoColor.values());
		model.addAttribute("colores", colorService.listar()); 
		return "carpetaColores/paginaColores"; 
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("color") ColorDTO colorDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("estados", EstadoColor.values());
			model.addAttribute("colores", colorService.listar()); 
			return "carpetaColores/paginaColores";
		}
		colorService.guardar(colorDto);
		return "redirect:/rutaColores/listar";
	}
	
	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("color") ColorDTO colorDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("estados", EstadoColor.values());
			model.addAttribute("colores", colorService.listar()); 
			return "carpetaColores/paginaColores";
		}
		colorService.actualiza(colorDto);
		return "redirect:/rutaColores/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable("uuid") UUID uuid) {
		model.addAttribute("color", colorService.obtenerColorUUID(uuid));
		model.addAttribute("estados", EstadoColor.values());
		model.addAttribute("colores", colorService.listar()); 
		return "carpetaColores/paginaColores";
	}
	
	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable("uuid") UUID uuid) {
		colorService.borrar(uuid);
		return "redirect:/rutaColores/listar";
	}
}