package com.utsem.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
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
import com.utsem.app.dto.ColorDTO;
import com.utsem.app.enums.EstadoColor;
import com.utsem.app.model.Color;
import com.utsem.app.repo.ColorRepo;
import com.utsem.app.seeder.DatabaseSeeder;
import com.utsem.app.service.ColorService;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ColorIntegrationTest {

    @MockitoBean
    private DatabaseConfig databaseConfig;

    @MockitoBean
    private DatabaseSeeder databaseSeeder;

    @Autowired
    private ColorService colorService;

    @Autowired
    private ColorRepo colorRepo;

    @BeforeEach
    void setUp() {
        // Limpiamos el repositorio antes de cada test
        colorRepo.deleteAll();
    }

    @Test
    @DisplayName("Guardar y listar color")
    void testGuardarYListarColor() {
        // Arrange
        ColorDTO dto = new ColorDTO();
        dto.setNombre("Rojo");
        dto.setEstadoColor(EstadoColor.Disponible);
        
        // Act
        colorService.guardar(dto);
        List<ColorDTO> colores = colorService.listar();
        
        // Assert
        assertThat(colores).hasSize(1);
        assertThat(colores.get(0).getNombre()).isEqualTo("Rojo");
    }

    @Test
    @DisplayName("Actualizar color existente")
    void testActualizarColor() {
        // Arrange
        ColorDTO dto = new ColorDTO();
        dto.setNombre("Azul");
        dto.setEstadoColor(EstadoColor.Disponible);
        colorService.guardar(dto);
        
        List<ColorDTO> colores = colorService.listar();
        ColorDTO guardado = colores.get(0);
        
        // Act
        guardado.setNombre("Azul Marino");
        colorService.actualiza(guardado);
        
        // Assert
        ColorDTO actualizado = colorService.obtenerColorUUID(guardado.getUuid());
        assertThat(actualizado.getNombre()).isEqualTo("Azul Marino");
    }

    @Test
    @DisplayName("Borrar color cambia estado a Descontinuado")
    void testBorrarColor_cambiaEstadoADescontinuado() {
        // Arrange
        ColorDTO dto = new ColorDTO();
        dto.setNombre("Negro");
        dto.setEstadoColor(EstadoColor.Disponible);
        colorService.guardar(dto);
        
        List<ColorDTO> colores = colorService.listar();
        UUID uuid = colores.get(0).getUuid();
        
        // Act
        colorService.borrar(uuid);
        
        // Assert
        Color color = colorRepo.findByUuid(uuid).orElseThrow();
        assertThat(color.getEstadoColor()).isEqualTo(EstadoColor.Descontinuado);
    }

    @Test
    @DisplayName("Obtener color con UUID inexistente lanza excepcion")
    void testObtenerColorPorUUID_noExistente_lanzaExcepcion() {
        // Arrange
        UUID random = UUID.randomUUID();
        
        // Act & Assert
        assertThatThrownBy(() -> colorService.obtenerColorUUID(random))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
