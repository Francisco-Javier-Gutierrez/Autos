package com.utsem.app.controller;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utsem.app.model.Color;
import com.utsem.app.model.DetProd;
import com.utsem.app.model.Pedido;
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
		List<DetProd> inventario = detProdService.listarEntidades();
		Map<Color, List<DetProd>> reporte = inventario.stream()
				.filter(det -> det.getColor() != null)
				.collect(Collectors.groupingBy(DetProd::getColor));

		model.addAttribute("reporte", reporte);
		return "carpetaReportes/disponibilidad";
	}

	@GetMapping("ventas-mes")
	public String metodoReporteVentasMes(Model model) {
		List<Pedido> pedidos = pedidoService.listarEntidades();
		Map<java.time.YearMonth, List<Pedido>> agrupadoPorMes = pedidos.stream()
				.filter(p -> p.getFechaPedido() != null)
				.collect(Collectors.groupingBy(
						p -> java.time.YearMonth.from(p.getFechaPedido()),
						() -> new java.util.TreeMap<java.time.YearMonth, List<Pedido>>(java.util.Comparator.reverseOrder()),
						Collectors.toList()
				));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));

		Map<String, List<Pedido>> reporte = new LinkedHashMap<>();
		agrupadoPorMes.forEach((ym, lista) -> {
			String mesLegible = ym.format(formatter);
			mesLegible = mesLegible.substring(0, 1).toUpperCase() + mesLegible.substring(1);
			reporte.put(mesLegible, lista);
		});

		model.addAttribute("reporte", reporte);
		return "carpetaReportes/ventasMes";
	}
}
