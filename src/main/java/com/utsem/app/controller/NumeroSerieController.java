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

import com.utsem.app.dto.NumeroSerieDTO;
import com.utsem.app.enums.EstadoUnidad;
import com.utsem.app.service.PedidoService;
import com.utsem.app.service.DetProdService;
import com.utsem.app.service.NumeroSerieService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("rutaNumerosSerie")
public class NumeroSerieController {

	@Autowired
	private NumeroSerieService numeroSerieService;

	@Autowired
	private DetProdService detProdService;

	@Autowired
	private PedidoService pedidoService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("series", numeroSerieService.listar());
		return "carpetaNumerosSerie/paginaNumerosSerie";
	}

	@GetMapping("listar/{detProdUuid}")
	public String metodoListarPorDetProd(Model model, @PathVariable UUID detProdUuid) {
		model.addAttribute("series", numeroSerieService.listarPorDetProd(detProdUuid));
		model.addAttribute("detProdUuid", detProdUuid);
		return "carpetaNumerosSerie/paginaNumerosSerie";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("serie", new NumeroSerieDTO());
		model.addAttribute("detalles", detProdService.listar());
		model.addAttribute("estadosUnidad", EstadoUnidad.values());
		model.addAttribute("pedidos", pedidoService.listar());
		return "carpetaNumerosSerie/paginaFormularioSerie";
	}

	@GetMapping("nuevo/{detProdUuid}")
	public String metodoNuevoConDetProd(Model model, @PathVariable UUID detProdUuid) {
		NumeroSerieDTO dto = new NumeroSerieDTO();
		dto.setDetProdUuid(detProdUuid);
		model.addAttribute("serie", dto);
		model.addAttribute("detalles", detProdService.listar());
		model.addAttribute("estadosUnidad", EstadoUnidad.values());
		model.addAttribute("pedidos", pedidoService.listar());
		return "carpetaNumerosSerie/paginaFormularioSerie";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("serie") NumeroSerieDTO dto, BindingResult result, Model model) {
		if (dto.getNumeroSerie() != null && !dto.getNumeroSerie().trim().isEmpty()) {
			if (numeroSerieService.existeNumeroSerie(dto.getNumeroSerie().trim())) {
				result.rejectValue("numeroSerie", "error.numeroSerie", "¡El número de serie ya está registrado!");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("detalles", detProdService.listar());
			model.addAttribute("estadosUnidad", EstadoUnidad.values());
			model.addAttribute("pedidos", pedidoService.listar());
			return "carpetaNumerosSerie/paginaFormularioSerie";
		}
		numeroSerieService.guardar(dto);
		return "redirect:/rutaNumerosSerie/listar";
	}

	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("serie") NumeroSerieDTO dto, BindingResult result, Model model) {
		if (dto.getNumeroSerie() != null && !dto.getNumeroSerie().trim().isEmpty() && dto.getUuid() != null) {
			if (numeroSerieService.existeNumeroSerieExcluyendo(dto.getNumeroSerie().trim(), dto.getUuid())) {
				result.rejectValue("numeroSerie", "error.numeroSerie", "¡El número de serie ya está registrado en otra unidad!");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("detalles", detProdService.listar());
			model.addAttribute("estadosUnidad", EstadoUnidad.values());
			model.addAttribute("pedidos", pedidoService.listar());
			return "carpetaNumerosSerie/paginaFormularioSerie";
		}
		numeroSerieService.actualiza(dto);
		return "redirect:/rutaNumerosSerie/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable UUID uuid) {
		model.addAttribute("serie", numeroSerieService.obtenerPorUuid(uuid));
		model.addAttribute("detalles", detProdService.listar());
		model.addAttribute("estadosUnidad", EstadoUnidad.values());
		model.addAttribute("pedidos", pedidoService.listar());
		return "carpetaNumerosSerie/paginaFormularioSerie";
	}

	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable UUID uuid) {
		numeroSerieService.borrar(uuid);
		return "redirect:/rutaNumerosSerie/listar";
	}
}
