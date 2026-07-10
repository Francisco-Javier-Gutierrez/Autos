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

	@Autowired
	private com.utsem.app.service.PedidoService pedidoService;

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

	@GetMapping("ventas-mes")
	public String metodoReporteVentasMes(Model model) {
		List<com.utsem.app.model.Pedido> pedidos = pedidoService.listarEntidades();

		// Agrupar por YearMonth ordenados de forma descendente (más recientes primero)
		Map<java.time.YearMonth, List<com.utsem.app.model.Pedido>> agrupadoPorMes = pedidos.stream()
				.filter(p -> p.getFechaPedido() != null)
				.collect(Collectors.groupingBy(
						p -> java.time.YearMonth.from(p.getFechaPedido()),
						() -> new java.util.TreeMap<java.time.YearMonth, List<com.utsem.app.model.Pedido>>(java.util.Comparator.reverseOrder()),
						Collectors.toList()
				));

		// Formateador de nombres de meses en español
		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("es", "ES"));

		// Enlazar con LinkedHashMap para mantener el orden cronológico
		Map<String, List<com.utsem.app.model.Pedido>> reporte = new java.util.LinkedHashMap<>();
		agrupadoPorMes.forEach((ym, lista) -> {
			String mesLegible = ym.format(formatter);
			mesLegible = mesLegible.substring(0, 1).toUpperCase() + mesLegible.substring(1);
			reporte.put(mesLegible, lista);
		});

		model.addAttribute("reporte", reporte);
		return "carpetaReportes/ventasMes";
	}
}
