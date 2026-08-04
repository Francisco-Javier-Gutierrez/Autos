package com.utsem.app.seeder;

import java.util.UUID;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.utsem.app.model.Cliente;
import com.utsem.app.model.Color;
import com.utsem.app.model.Producto;
import com.utsem.app.model.DetProd;
import com.utsem.app.repo.ClienteRepo;
import com.utsem.app.repo.ColorRepo;
import com.utsem.app.repo.ProductoRepo;
import com.utsem.app.repo.DetProdRepo;
import com.utsem.app.enums.EstadoColor;
import com.utsem.app.enums.Estatus;
import com.utsem.app.enums.Condicion;
import com.utsem.app.enums.Transmision;
import com.utsem.app.enums.EstadoUnidad;
import com.utsem.app.repo.NumeroSerieRepo;
import com.utsem.app.model.NumeroSerie;
import java.time.LocalDate;

@Component
public class DatabaseSeeder implements CommandLineRunner {

	@Autowired
	private ClienteRepo clienteRepo;

	@Autowired
	private ColorRepo colorRepo;

	@Autowired
	private ProductoRepo productoRepo;

	@Autowired
	private DetProdRepo detProdRepo;

	@Autowired
	private NumeroSerieRepo numeroSerieRepo;

	@Override
	public void run(String... args) throws Exception {
		seedClientes();
		seedColores();
		seedProductos();
		seedDetalles();
		seedNumerosSerie();
	}

	private void seedClientes() {
		crearClienteSiNoExiste("Francisco Javier Gutierrez Herculano", "chinopaco.05@gmail.com", "7224103554");
		crearClienteSiNoExiste("Perla Manuel Morales", "perlitaz837@gmail.com", "7225629812");
		crearClienteSiNoExiste("Rubi Flores Aguilar", "floresrubi865@gmail.com", "7228050788");
		crearClienteSiNoExiste("Francely Anaid Giles Hernández", "frans24-gil@hotmail.com", "5534617506");
	}

	private void crearClienteSiNoExiste(String nombre, String correo, String telefono) {
		if (clienteRepo.findByCorreo(correo).isEmpty()) {
			Cliente cliente = new Cliente();
			cliente.setUuid(UUID.randomUUID());
			cliente.setNombre(nombre);
			cliente.setCorreo(correo);
			cliente.setTelefono(telefono);
			clienteRepo.save(cliente);
			System.out.println("Cliente seeded: " + nombre);
		}
	}

	private void seedColores() {
		crearColorSiNoExiste("Rojo", EstadoColor.Disponible);
		crearColorSiNoExiste("Negro", EstadoColor.Disponible);
		crearColorSiNoExiste("Blanco", EstadoColor.Disponible);
		crearColorSiNoExiste("Gris", EstadoColor.Disponible);
		crearColorSiNoExiste("Azul", EstadoColor.Disponible);
		crearColorSiNoExiste("Plata", EstadoColor.Disponible);
		crearColorSiNoExiste("Verde", EstadoColor.Disponible);
		crearColorSiNoExiste("Amarillo", EstadoColor.Disponible);
		crearColorSiNoExiste("Naranja", EstadoColor.Disponible);
		crearColorSiNoExiste("Marrón", EstadoColor.Disponible);
	}

	private void crearColorSiNoExiste(String nombre, EstadoColor estadoColor) {
		boolean existe = colorRepo.findAll().stream().anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre));
		if (!existe) {
			Color color = new Color();
			color.setUuid(UUID.randomUUID());
			color.setNombre(nombre);
			color.setEstadoColor(estadoColor);
			colorRepo.save(color);
			System.out.println("Color seeded: " + nombre);
		}
	}

	private void seedProductos() {
		crearProductoSiNoExiste("Toyota", "SE", "Corolla", 450000.0, 2023, Estatus.Disponible, Condicion.Nuevo, 5, true,
				"Excelente rendimiento y durabilidad", 3);
		crearProductoSiNoExiste("Honda", "Touring", "Civic", 480000.0, 2022, Estatus.Disponible, Condicion.Seminuevo, 4,
				false, "Muy cómodo y tecnológico", 2);
		crearProductoSiNoExiste("Nissan", "Exclusive", "Versa", 320000.0, 2021, Estatus.Disponible, Condicion.Usado, 4,
				false, "Económico en combustible", 1);
		crearProductoSiNoExiste("Ford", "GT", "Mustang", 980000.0, 2024, Estatus.Disponible, Condicion.Nuevo, 5, true,
				"Gran potencia y diseño icónico", 3);
		crearProductoSiNoExiste("Chevrolet", "SS", "Camaro", 1100000.0, 2024, Estatus.Disponible, Condicion.Nuevo, 5,
				true, "Deportivo americano por excelencia", 3);
		crearProductoSiNoExiste("Mazda", "i Grand Touring", "3", 420000.0, 2023, Estatus.Disponible,
				Condicion.Seminuevo, 4, false, "Excelente manejo y acabados interiores", 2);
		crearProductoSiNoExiste("Volkswagen", "Highline", "Jetta", 460000.0, 2024, Estatus.Disponible, Condicion.Nuevo,
				4, true, "Clásico sedán muy confiable", 2);
		crearProductoSiNoExiste("Kia", "EX", "Rio", 290000.0, 2020, Estatus.Disponible, Condicion.Usado, 3, false,
				"Económico y práctico para la ciudad", 1);
		crearProductoSiNoExiste("BMW", "M Sport", "Serie 3", 950000.0, 2022, Estatus.Disponible, Condicion.Seminuevo, 5,
				true, "Lujo y deportividad excepcionales", 3);
	}

	private void crearProductoSiNoExiste(String marca, String subMarca, String modelo, Double precio, Integer anio,
			Estatus est, Condicion cond, Integer val, Boolean fav, String res, Integer intLvl) {
		boolean existe = productoRepo.findAll().stream()
				.anyMatch(p -> p.getMarca().equalsIgnoreCase(marca) && p.getModelo().equalsIgnoreCase(modelo));
		if (!existe) {
			Producto producto = new Producto();
			producto.setUuid(UUID.randomUUID());
			producto.setMarca(marca);
			producto.setSubMarca(subMarca);
			producto.setModelo(modelo);
			producto.setPrecio(precio);
			producto.setAnio(anio);
			producto.setEstado(est);
			producto.setCondicion(cond);
			producto.setValoracionRubiFA(val);
			producto.setFavoritoPerlaMM(fav);
			producto.setResenaFrancelyAnaidGH(res);
			producto.setNivelInteresFranciscoJavierGH(intLvl);
			productoRepo.save(producto);
			System.out.println("Producto seeded: " + marca + " " + modelo);
		}
	}

	private void seedDetalles() {
		if (detProdRepo.count() == 0) {
			List<Producto> productos = productoRepo.findAll();
			List<Color> colores = colorRepo.findAll();

			if (!productos.isEmpty() && !colores.isEmpty()) {
				Producto corolla = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Corolla")).findFirst()
						.orElse(productos.get(0));
				Producto civic = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Civic")).findFirst()
						.orElse(productos.get(0));
				Producto versa = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Versa")).findFirst()
						.orElse(productos.get(0));
				Producto mustang = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Mustang")).findFirst()
						.orElse(productos.get(0));
				Producto camaro = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Camaro")).findFirst()
						.orElse(productos.get(0));
				Producto mazda3 = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("3")).findFirst()
						.orElse(productos.get(0));
				Producto jetta = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Jetta")).findFirst()
						.orElse(productos.get(0));
				Producto rio = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Rio")).findFirst()
						.orElse(productos.get(0));
				Producto serie3 = productos.stream().filter(p -> p.getModelo().equalsIgnoreCase("Serie 3")).findFirst()
						.orElse(productos.get(0));

				Color rojo = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Rojo")).findFirst()
						.orElse(colores.get(0));
				Color negro = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Negro")).findFirst()
						.orElse(colores.get(0));
				Color blanco = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Blanco")).findFirst()
						.orElse(colores.get(0));
				Color gris = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Gris")).findFirst()
						.orElse(colores.get(0));
				Color azul = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Azul")).findFirst()
						.orElse(colores.get(0));
				Color plata = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Plata")).findFirst()
						.orElse(colores.get(0));
				Color verde = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Verde")).findFirst()
						.orElse(colores.get(0));
				Color amarillo = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Amarillo")).findFirst()
						.orElse(colores.get(0));
				Color naranja = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Naranja")).findFirst()
						.orElse(colores.get(0));
				Color marron = colores.stream().filter(c -> c.getNombre().equalsIgnoreCase("Marrón")).findFirst()
						.orElse(colores.get(0));

				crearDetProd(corolla, rojo, 5, Transmision.Automatica);
				crearDetProd(corolla, blanco, 3, Transmision.Automatica);
				crearDetProd(civic, negro, 2, Transmision.Automatica);
				crearDetProd(civic, gris, 4, Transmision.Automatica);
				crearDetProd(versa, azul, 6, Transmision.Manual);
				crearDetProd(mustang, rojo, 2, Transmision.Manual);
				crearDetProd(mustang, negro, 1, Transmision.Automatica);

				crearDetProd(camaro, amarillo, 2, Transmision.Automatica);
				crearDetProd(camaro, naranja, 1, Transmision.Manual);
				crearDetProd(mazda3, rojo, 4, Transmision.Automatica);
				crearDetProd(mazda3, plata, 3, Transmision.Manual);
				crearDetProd(jetta, blanco, 5, Transmision.Automatica);
				crearDetProd(jetta, gris, 3, Transmision.Automatica);
				crearDetProd(rio, azul, 2, Transmision.Manual);
				crearDetProd(rio, verde, 1, Transmision.Automatica);
				crearDetProd(serie3, negro, 2, Transmision.Automatica);
				crearDetProd(serie3, plata, 1, Transmision.Automatica);
				crearDetProd(serie3, marron, 1, Transmision.Automatica);
			}
		}
	}

	private void crearDetProd(Producto producto, Color color, Integer stock, Transmision transmision) {
		DetProd det = new DetProd();
		det.setUuid(UUID.randomUUID());
		det.setProducto(producto);
		det.setColor(color);
		det.setStock(stock);
		det.setTransmision(transmision);
		detProdRepo.save(det);
		System.out
				.println("Detalle de producto (Stock) seeded: " + producto.getMarca() + " (" + color.getNombre() + ")");
	}

	private void seedNumerosSerie() {
		if (numeroSerieRepo.count() == 0) {
			List<DetProd> detalles = detProdRepo.findAll();
			if (!detalles.isEmpty()) {
				for (DetProd det : detalles) {
					int cantidadSeries = (int) (Math.random() * 8); // Genera un número aleatorio de 0 a 7
					for (int j = 0; j < cantidadSeries; j++) {
						String vin = "VIN" + det.getId() + "A" + j + "R" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
						crearNumeroSerie(det, vin, EstadoUnidad.Disponible, "Lote Principal", null);
					}
					
					// Agregamos opcionalmente uno vendido y uno en mantenimiento por variedad
					String vinSold = "VIN" + det.getId() + "SOLD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
					crearNumeroSerie(det, vinSold, EstadoUnidad.Vendido, "Entregado", null);
					
					String vinMaint = "VIN" + det.getId() + "MAINT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
					crearNumeroSerie(det, vinMaint, EstadoUnidad.En_Mantenimiento, "Taller Central", null);
				}
				
				// Re-sincronizar el stock lógicamente de todas las variantes
				for (DetProd det : detProdRepo.findAll()) {
					long disp = numeroSerieRepo.countByDetProdIdAndEstadoUnidad(det.getId(), EstadoUnidad.Disponible);
					det.setStock((int) disp);
					detProdRepo.save(det);
				}
			}
		}
	}
	
	private void crearNumeroSerie(DetProd det, String serial, EstadoUnidad estado, String ubicacion, String observaciones) {
		NumeroSerie ns = new NumeroSerie();
		ns.setUuid(UUID.randomUUID());
		ns.setDetProd(det);
		ns.setNumeroSerie(serial);
		ns.setEstadoUnidad(estado);
		ns.setFechaIngreso(LocalDate.now().minusDays(10));
		ns.setUbicacion(ubicacion);
		ns.setObservaciones(observaciones);
		numeroSerieRepo.save(ns);
		System.out.println("Numero de Serie seeded: " + serial);
	}
}