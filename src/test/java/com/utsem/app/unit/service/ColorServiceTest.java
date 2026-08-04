package com.utsem.app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
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

import com.utsem.app.dto.ColorDTO;
import com.utsem.app.enums.EstadoColor;
import com.utsem.app.model.Color;
import com.utsem.app.repo.ColorRepo;
import com.utsem.app.service.ColorService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Pruebas unitarias para ColorService.
 * Se simulan las dependencias (ColorRepo y ModelMapper)
 * para probar únicamente la lógica del servicio.
 */
@ExtendWith(MockitoExtension.class)
class ColorServiceTest {

    @Mock
    private ColorRepo colorRepo;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ColorService colorService;

    // =============================================
    // PRUEBA: Listar colores devuelve lista de DTOs
    // =============================================
    @Test
    @DisplayName("Listar colores devuelve una lista de ColorDTO")
    void testListar_devuelveListaDeColoresDTO() {
        // Arrange
        Color color1 = new Color();
        color1.setId(1L);
        color1.setNombre("Rojo");
        color1.setEstadoColor(EstadoColor.Disponible);

        Color color2 = new Color();
        color2.setId(2L);
        color2.setNombre("Azul");
        color2.setEstadoColor(EstadoColor.Disponible);

        ColorDTO dto1 = new ColorDTO();
        dto1.setNombre("Rojo");
        ColorDTO dto2 = new ColorDTO();
        dto2.setNombre("Azul");

        when(colorRepo.findAll()).thenReturn(Arrays.asList(color1, color2));
        when(mapper.map(color1, ColorDTO.class)).thenReturn(dto1);
        when(mapper.map(color2, ColorDTO.class)).thenReturn(dto2);

        // Act
        List<ColorDTO> resultado = colorService.listar();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Rojo", resultado.get(0).getNombre());
        assertEquals("Azul", resultado.get(1).getNombre());
    }

    // =============================================
    // PRUEBA: Guardar color correctamente
    // =============================================
    @Test
    @DisplayName("Guardar un color nuevo correctamente")
    void testGuardar_guardaColorCorrectamente() {
        // Arrange
        ColorDTO dto = new ColorDTO();
        dto.setNombre("Verde");
        dto.setEstadoColor(EstadoColor.Disponible);

        Color entidad = new Color();
        entidad.setNombre("Verde");

        when(mapper.map(dto, Color.class)).thenReturn(entidad);

        // Act
        colorService.guardar(dto);

        // Assert - Verificamos que se llamó a save
        verify(colorRepo, times(1)).save(entidad);
    }

    // =============================================
    // PRUEBA: Actualizar un color existente
    // =============================================
    @Test
    @DisplayName("Actualizar un color existente correctamente")
    void testActualiza_colorExistente_actualizaCorrectamente() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ColorDTO dto = new ColorDTO();
        dto.setUuid(uuid);
        dto.setNombre("Rojo Actualizado");
        dto.setEstadoColor(EstadoColor.Disponible);

        Color existente = new Color();
        existente.setId(1L);
        existente.setUuid(uuid);
        existente.setNombre("Rojo");

        when(colorRepo.findByUuid(uuid)).thenReturn(Optional.of(existente));

        // Act
        colorService.actualiza(dto);

        // Assert
        verify(mapper, times(1)).map(dto, existente);
        verify(colorRepo, times(1)).save(existente);
    }

    // =============================================
    // PRUEBA: Actualizar color no existente lanza excepción
    // =============================================
    @Test
    @DisplayName("Actualizar color no existente lanza EntityNotFoundException")
    void testActualiza_colorNoExistente_lanzaExcepcion() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ColorDTO dto = new ColorDTO();
        dto.setUuid(uuid);

        when(colorRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> colorService.actualiza(dto));
    }

    // =============================================
    // PRUEBA: Borrar color cambia estado a Descontinuado (soft delete)
    // =============================================
    @Test
    @DisplayName("Borrar color cambia estadoColor a 'Descontinuado' (soft delete)")
    void testBorrar_cambiaEstadoColorADescontinuado() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        Color color = new Color();
        color.setId(1L);
        color.setUuid(uuid);
        color.setEstadoColor(EstadoColor.Disponible);

        when(colorRepo.findByUuid(uuid)).thenReturn(Optional.of(color));

        // Act
        colorService.borrar(uuid);

        // Assert - Verificamos que el estado cambió a Descontinuado
        assertEquals(EstadoColor.Descontinuado, color.getEstadoColor(),
            "El estado del color debe cambiar a 'Descontinuado'");
        verify(colorRepo, times(1)).save(color);
    }

    // =============================================
    // PRUEBA: Borrar color no existente lanza excepción
    // =============================================
    @Test
    @DisplayName("Borrar color no existente lanza EntityNotFoundException")
    void testBorrar_colorNoExistente_lanzaExcepcion() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        when(colorRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> colorService.borrar(uuid));
    }

    // =============================================
    // PRUEBA: Obtener color por UUID existente
    // =============================================
    @Test
    @DisplayName("Obtener color por UUID existente retorna ColorDTO")
    void testObtenerColorUUID_existente_retornaDTO() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        Color color = new Color();
        color.setUuid(uuid);
        color.setNombre("Negro");

        ColorDTO dtoEsperado = new ColorDTO();
        dtoEsperado.setNombre("Negro");

        when(colorRepo.findByUuid(uuid)).thenReturn(Optional.of(color));
        when(mapper.map(color, ColorDTO.class)).thenReturn(dtoEsperado);

        // Act
        ColorDTO resultado = colorService.obtenerColorUUID(uuid);

        // Assert
        assertNotNull(resultado);
        assertEquals("Negro", resultado.getNombre());
    }

    // =============================================
    // PRUEBA: Obtener color por UUID no existente
    // =============================================
    @Test
    @DisplayName("Obtener color por UUID inexistente lanza excepción")
    void testObtenerColorUUID_noExistente_lanzaExcepcion() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        when(colorRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> colorService.obtenerColorUUID(uuid));
    }
}
