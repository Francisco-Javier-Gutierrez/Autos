package com.utsem.app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utsem.app.dto.DetProdDTO;
import com.utsem.app.service.DetProdService;
import com.utsem.app.service.ProductoService;
import com.utsem.app.service.ColorService;
import com.utsem.app.service.NumeroSerieService;
import com.utsem.app.enums.Transmision;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("rutaDetalles")
public class DetProdController {

	@Autowired
	private DetProdService detProdService;

	@Autowired
	private ProductoService productoService;

	@Autowired
	private ColorService colorService;

	@Autowired
	private NumeroSerieService numeroSerieService;

	@GetMapping("listar")
	public String metodoListar(Model model) {
		model.addAttribute("detalles", detProdService.listar());
		return "carpetaDetalles/paginaDetalles";
	}

	@GetMapping("nuevo")
	public String metodoNuevo(Model model) {
		model.addAttribute("detalle", new DetProdDTO());
		model.addAttribute("productos", productoService.listarEntidades());
		model.addAttribute("colores", colorService.listar());
		model.addAttribute("transmisiones", Transmision.values());
		return "carpetaDetalles/paginaFormularioDetalle";
	}

	@PostMapping("guardar")
	public String metodoGuarda(@Valid @ModelAttribute("detalle") DetProdDTO detDto, BindingResult result, Model model) {
		if (detDto.getProductoUuid() != null && detDto.getColorUuid() != null && detDto.getTransmision() != null) {
			java.util.Optional<com.utsem.app.model.DetProd> dup = detProdService.buscarDuplicado(
					detDto.getProductoUuid(), detDto.getColorUuid(), detDto.getTransmision());
			if (dup.isPresent()) {
				result.rejectValue("transmision", "error.transmision", "¡Ya existe un registro de inventario con este vehículo, color y transmisión!");
			}
		}

		if (detDto.getNumerosSerieNuevos() != null && !detDto.getNumerosSerieNuevos().trim().isEmpty()) {
			String[] serials = detDto.getNumerosSerieNuevos().split("[,\\n]+");
			for (String serial : serials) {
				String s = serial.trim();
				if (!s.isEmpty() && numeroSerieService.existeNumeroSerie(s)) {
					result.rejectValue("numerosSerieNuevos", "error.numerosSerieNuevos", 
							"¡El número de serie '" + s + "' ya está registrado en el sistema!");
					break;
				}
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("productos", productoService.listarEntidades());
			model.addAttribute("colores", colorService.listar());
			model.addAttribute("transmisiones", Transmision.values());
			return "carpetaDetalles/paginaFormularioDetalle";
		}
		detProdService.guardar(detDto);
		return "redirect:/rutaDetalles/listar";
	}

	@PostMapping("actualizar")
	public String metodoActualiza(@Valid @ModelAttribute("detalle") DetProdDTO detDto, BindingResult result, Model model) {
		if (detDto.getProductoUuid() != null && detDto.getColorUuid() != null && detDto.getTransmision() != null && detDto.getUuid() != null) {
			java.util.Optional<com.utsem.app.model.DetProd> dup = detProdService.buscarDuplicado(
					detDto.getProductoUuid(), detDto.getColorUuid(), detDto.getTransmision());
			if (dup.isPresent() && !dup.get().getUuid().equals(detDto.getUuid())) {
				result.rejectValue("transmision", "error.transmision", "¡Ya existe otro registro de inventario con este vehículo, color y transmisión!");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("productos", productoService.listarEntidades());
			model.addAttribute("colores", colorService.listar());
			model.addAttribute("transmisiones", Transmision.values());
			return "carpetaDetalles/paginaFormularioDetalle";
		}
		detProdService.actualiza(detDto);
		return "redirect:/rutaDetalles/listar";
	}

	@GetMapping("editar/{uuid}")
	public String metodoEditar(Model model, @PathVariable UUID uuid) {
		model.addAttribute("detalle", detProdService.obtenerPorUuid(uuid));
		model.addAttribute("productos", productoService.listarEntidades());
		model.addAttribute("colores", colorService.listar());
		model.addAttribute("transmisiones", Transmision.values());
		return "carpetaDetalles/paginaFormularioDetalle";
	}

	@GetMapping("eliminar/{uuid}")
	public String metodoElimina(@PathVariable UUID uuid) {
		detProdService.borrar(uuid);
		return "redirect:/rutaDetalles/listar";
	}

	@GetMapping("validar-duplicado")
	@ResponseBody
	public java.util.Map<String, Object> validarDuplicado(
			@RequestParam("productoUuid") UUID productoUuid,
			@RequestParam("colorUuid") UUID colorUuid,
			@RequestParam("transmision") Transmision transmision) {
		
		java.util.Map<String, Object> response = new java.util.HashMap<>();
		java.util.Optional<com.utsem.app.model.DetProd> duplicate = detProdService.buscarDuplicado(productoUuid, colorUuid, transmision);
		if (duplicate.isPresent()) {
			response.put("exists", true);
			response.put("uuid", duplicate.get().getUuid());
			response.put("stock", duplicate.get().getStock());
		} else {
			response.put("exists", false);
		}
		return response;
	}

	@PostMapping("agregar-stock")
	@ResponseBody
	public java.util.Map<String, Object> agregarStock(
			@RequestParam("uuid") UUID uuid,
			@RequestParam("series") String series) {
		
		java.util.Map<String, Object> response = new java.util.HashMap<>();
		try {
			detProdService.agregarStockConSeries(uuid, series);
			response.put("success", true);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", e.getMessage());
		}
		return response;
	}
}