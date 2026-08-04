package com.utsem.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.utsem.app.config.DatabaseConfig;
import com.utsem.app.dto.ProductoDTO;
import com.utsem.app.enums.Condicion;
import com.utsem.app.enums.Estatus;
import com.utsem.app.model.Producto;
import com.utsem.app.repo.ProductoRepo;
import com.utsem.app.seeder.DatabaseSeeder;
import com.utsem.app.service.ProductoService;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductoIntegrationTest {

    @MockitoBean
    private DatabaseConfig databaseConfig;

    @MockitoBean
    private DatabaseSeeder databaseSeeder;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepo productoRepo;

    @BeforeEach
    void setUp() {
        // Limpiamos el repositorio antes de cada test
        productoRepo.deleteAll();
    }

    @Test
    @DisplayName("Guardar y listar producto")
    void testGuardarYListarProducto() {
        // Arrange
        ProductoDTO dto = new ProductoDTO();
        dto.setMarca("Toyota");
        dto.setSubMarca("Corolla");
        dto.setModelo("LE");
        dto.setAnio(2022);
        dto.setPrecio(25000.0);
        dto.setEstado(Estatus.Disponible);
        dto.setCondicion(Condicion.Nuevo);
        
        // Act
        productoService.guardar(dto);
        List<ProductoDTO> productos = productoService.listar();
        
        // Assert
        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).getMarca()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("Actualizar producto existente")
    void testActualizarProducto() {
        // Arrange
        ProductoDTO dto = new ProductoDTO();
        dto.setMarca("Honda");
        dto.setSubMarca("Civic");
        dto.setModelo("EX");
        dto.setAnio(2021);
        dto.setPrecio(22000.0);
        dto.setEstado(Estatus.Disponible);
        dto.setCondicion(Condicion.Seminuevo);
        productoService.guardar(dto);
        
        List<ProductoDTO> productos = productoService.listar();
        ProductoDTO guardado = productos.get(0);
        
        // Act
        guardado.setPrecio(23000.0);
        productoService.actualiza(guardado);
        
        // Assert
        ProductoDTO actualizado = productoService.obtenerProductoUUID(guardado.getUuid());
        assertThat(actualizado.getPrecio()).isEqualTo(23000.0);
    }

    @Test
    @DisplayName("Borrar producto cambia estado a Descontinuado")
    void testBorrarProducto_cambiaEstadoADescontinuado() {
        // Arrange
        ProductoDTO dto = new ProductoDTO();
        dto.setMarca("Nissan");
        dto.setSubMarca("Sentra");
        dto.setModelo("SV");
        dto.setAnio(2020);
        dto.setPrecio(18000.0);
        dto.setEstado(Estatus.Disponible);
        dto.setCondicion(Condicion.Usado);
        productoService.guardar(dto);
        
        List<ProductoDTO> productos = productoService.listar();
        UUID uuid = productos.get(0).getUuid();
        
        // Act
        productoService.borrar(uuid);
        
        // Assert
        Producto producto = productoRepo.findByUuid(uuid).orElseThrow();
        assertThat(producto.getEstado()).isEqualTo(Estatus.Descontinuado);
    }

    @Test
    @DisplayName("Obtener producto con UUID inexistente lanza excepcion")
    void testObtenerProductoPorUUID_noExistente_lanzaExcepcion() {
        // Arrange
        UUID random = UUID.randomUUID();
        
        // Act & Assert
        assertThatThrownBy(() -> productoService.obtenerProductoUUID(random))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Buscar duplicado cuando existe")
    void testBuscarDuplicado_existeDuplicado() {
        // Arrange
        ProductoDTO dto = new ProductoDTO();
        dto.setMarca("Ford");
        dto.setSubMarca("Mustang");
        dto.setModelo("GT");
        dto.setAnio(2023);
        dto.setPrecio(45000.0);
        dto.setEstado(Estatus.Disponible);
        dto.setCondicion(Condicion.Nuevo);
        productoService.guardar(dto);
        
        // Act
        Optional<Producto> duplicado = productoService.buscarDuplicado("Ford", "Mustang", "GT", 2023, Condicion.Nuevo);
        
        // Assert
        assertThat(duplicado).isPresent();
    }

    @Test
    @DisplayName("Buscar duplicado cuando no existe")
    void testBuscarDuplicado_noExisteDuplicado() {
        // Act
        Optional<Producto> duplicado = productoService.buscarDuplicado("Ford", "Fiesta", "S", 2019, Condicion.Usado);
        
        // Assert
        assertThat(duplicado).isNotPresent();
    }
}
