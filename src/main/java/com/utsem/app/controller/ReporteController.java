package com.utsem.app.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utsem.app.model.Color;
import com.utsem.app.model.DetProd;
import com.utsem.app.service.DetProdService;

@Controller
@RequestMapping("rutaReportes")
public class ReporteController {

	@Autowired
	private DetProdService detProdService;

	@GetMapping("disponibilidad")
	public String metodoReporteDisponibilidad(Model model) {
		// Obtener todos los registros de inventario (DetProd)
		List<DetProd> inventario = detProdService.listarEntidades();

		// Agrupar por color (filtrando que el color no sea nulo)
		Map<Color, List<DetProd>> reporte = inventario.stream()
				.filter(det -> det.getColor() != null)
				.collect(Collectors.groupingBy(DetProd::getColor));

		model.addAttribute("reporte", reporte);
		return "carpetaReportes/disponibilidad";
	}
}
