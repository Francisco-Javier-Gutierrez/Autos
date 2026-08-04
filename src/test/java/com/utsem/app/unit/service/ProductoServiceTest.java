package com.utsem.app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.utsem.app.dto.ProductoDTO;
import com.utsem.app.enums.Condicion;
import com.utsem.app.enums.Estatus;
import com.utsem.app.model.Producto;
import com.utsem.app.repo.ProductoRepo;
import com.utsem.app.service.ProductoService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Pruebas unitarias para ProductoService.
 * Se simulan las dependencias (ProductoRepo y ModelMapper)
 * para probar la lógica de negocio de productos de forma aislada.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepo productoRepo;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ProductoService productoService;

    // =============================================
    // PRUEBA: Listar productos devuelve lista de DTOs
    // =============================================
    @Test
    @DisplayName("Listar productos devuelve una lista de ProductoDTO")
    void testListar_devuelveListaDeProductosDTO() {
        // Arrange
        Producto prod1 = new Producto();
        prod1.setId(1L);
        prod1.setMarca("Toyota");
        prod1.setModelo("Corolla");

        Producto prod2 = new Producto();
        prod2.setId(2L);
        prod2.setMarca("Honda");
        prod2.setModelo("Civic");

        ProductoDTO dto1 = new ProductoDTO();
        dto1.setMarca("Toyota");
        ProductoDTO dto2 = new ProductoDTO();
        dto2.setMarca("Honda");

        when(productoRepo.findAll()).thenReturn(Arrays.asList(prod1, prod2));
        when(mapper.map(prod1, ProductoDTO.class)).thenReturn(dto1);
        when(mapper.map(prod2, ProductoDTO.class)).thenReturn(dto2);

        // Act
        List<ProductoDTO> resultado = productoService.listar();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Toyota", resultado.get(0).getMarca());
        assertEquals("Honda", resultado.get(1).getMarca());
    }

    // =============================================
    // PRUEBA: Guardar producto correctamente
    // =============================================
    @Test
    @DisplayName("Guardar un producto nuevo correctamente")
    void testGuardar_guardaProductoCorrectamente() {
        // Arrange
        ProductoDTO dto = new ProductoDTO();
        dto.setMarca("Ford");
        dto.setModelo("Mustang");
        dto.setPrecio(980000.0);

        Producto entidad = new Producto();
        entidad.setMarca("Ford");
        entidad.setModelo("Mustang");

        when(mapper.map(dto, Producto.class)).thenReturn(entidad);

        // Act
        productoService.guardar(dto);

        // Assert
        verify(productoRepo, times(1)).save(entidad);
    }

    // =============================================
    // PRUEBA: Actualizar producto existente
    // =============================================
    @Test
    @DisplayName("Actualizar un producto existente correctamente")
    void testActualiza_productoExistente_actualizaCorrectamente() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ProductoDTO dto = new ProductoDTO();
        dto.setUuid(uuid);
        dto.setMarca("Toyota Actualizado");

        Producto existente = new Producto();
        existente.setId(1L);
        existente.setUuid(uuid);
        existente.setMarca("Toyota");

        when(productoRepo.findByUuid(uuid)).thenReturn(Optional.of(existente));

        // Act
        productoService.actualiza(dto);

        // Assert
        verify(mapper, times(1)).map(dto, existente);
        verify(productoRepo, times(1)).save(existente);
    }

    // =============================================
    // PRUEBA: Actualizar producto no existente lanza excepción
    // =============================================
    @Test
    @DisplayName("Actualizar producto no existente lanza EntityNotFoundException")
    void testActualiza_productoNoExistente_lanzaExcepcion() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ProductoDTO dto = new ProductoDTO();
        dto.setUuid(uuid);

        when(productoRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> productoService.actualiza(dto));
    }

    // =============================================
    // PRUEBA: Obtener producto por UUID existente
    // =============================================
    @Test
    @DisplayName("Obtener producto por UUID existente retorna ProductoDTO")
    void testObtenerProductoUUID_existente_retornaDTO() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        Producto entidad = new Producto();
        entidad.setUuid(uuid);
        entidad.setMarca("Mazda");

        ProductoDTO dtoEsperado = new ProductoDTO();
        dtoEsperado.setMarca("Mazda");

        when(productoRepo.findByUuid(uuid)).thenReturn(Optional.of(entidad));
        when(mapper.map(entidad, ProductoDTO.class)).thenReturn(dtoEsperado);

        // Act
        ProductoDTO resultado = productoService.obtenerProductoUUID(uuid);

        // Assert
        assertNotNull(resultado);
        assertEquals("Mazda", resultado.getMarca());
    }

    // =============================================
    // PRUEBA: Obtener producto por UUID no existente
    // =============================================
    @Test
    @DisplayName("Obtener producto por UUID inexistente lanza excepción")
    void testObtenerProductoUUID_noExistente_lanzaExcepcion() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        when(productoRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> productoService.obtenerProductoUUID(uuid));
    }

    // =============================================
    // PRUEBA: Borrar producto cambia estado a Descontinuado (soft delete)
    // =============================================
    @Test
    @DisplayName("Borrar producto cambia su estado a 'Descontinuado' (soft delete)")
    void testBorrar_cambiaEstadoADescontinuado() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setUuid(uuid);
        producto.setEstado(Estatus.Disponible);

        when(productoRepo.findByUuid(uuid)).thenReturn(Optional.of(producto));

        // Act
        productoService.borrar(uuid);

        // Assert
        assertEquals(Estatus.Descontinuado, producto.getEstado(),
            "El estado del producto debe cambiar a 'Descontinuado'");
        verify(productoRepo, times(1)).save(producto);
    }

    // =============================================
    // PRUEBA: Buscar duplicado con parámetros nulos retorna vacío
    // =============================================
    @Test
    @DisplayName("Buscar duplicado con parámetros nulos retorna Optional vacío")
    void testBuscarDuplicado_conParametrosNulos_retornaEmpty() {
        // Act & Assert - Si algún parámetro es null, debe retornar vacío
        Optional<Producto> resultado = productoService.buscarDuplicado(null, "SE", "Corolla", 2023, Condicion.Nuevo);
        assertTrue(resultado.isEmpty(), "Debe retornar vacío si la marca es null");

        resultado = productoService.buscarDuplicado("Toyota", null, "Corolla", 2023, Condicion.Nuevo);
        assertTrue(resultado.isEmpty(), "Debe retornar vacío si la submarca es null");
    }

    // =============================================
    // PRUEBA: Buscar duplicado que SÍ existe
    // =============================================
    @Test
    @DisplayName("Buscar duplicado existente retorna el producto")
    void testBuscarDuplicado_existeDuplicado_retornaProducto() {
        // Arrange
        Producto existente = new Producto();
        existente.setId(1L);
        existente.setMarca("Toyota");
        existente.setSubMarca("SE");
        existente.setModelo("Corolla");
        existente.setAnio(2023);
        existente.setCondicion(Condicion.Nuevo);
        existente.setEstado(Estatus.Disponible);

        when(productoRepo.findAll()).thenReturn(Collections.singletonList(existente));

        // Act
        Optional<Producto> resultado = productoService.buscarDuplicado("Toyota", "SE", "Corolla", 2023, Condicion.Nuevo);

        // Assert
        assertTrue(resultado.isPresent(), "Debe encontrar el duplicado");
        assertEquals("Toyota", resultado.get().getMarca());
    }

    // =============================================
    // PRUEBA: Buscar duplicado que NO existe
    // =============================================
    @Test
    @DisplayName("Buscar duplicado no existente retorna Optional vacío")
    void testBuscarDuplicado_noExisteDuplicado_retornaEmpty() {
        // Arrange
        Producto existente = new Producto();
        existente.setId(1L);
        existente.setMarca("Toyota");
        existente.setSubMarca("SE");
        existente.setModelo("Corolla");
        existente.setAnio(2023);
        existente.setCondicion(Condicion.Nuevo);
        existente.setEstado(Estatus.Disponible);

        when(productoRepo.findAll()).thenReturn(Collections.singletonList(existente));

        // Act - Buscamos con datos diferentes
        Optional<Producto> resultado = productoService.buscarDuplicado("Honda", "Touring", "Civic", 2022, Condicion.Seminuevo);

        // Assert
        assertTrue(resultado.isEmpty(), "No debe encontrar duplicado con datos diferentes");
    }
}
