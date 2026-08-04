package com.utsem.app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.utsem.app.dto.ClienteDTO;
import com.utsem.app.enums.EstatusCliente;
import com.utsem.app.model.Cliente;
import com.utsem.app.repo.ClienteRepo;
import com.utsem.app.service.ClienteService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Pruebas unitarias para ClienteService.
 * Usa Mockito para simular (mock) las dependencias del servicio
 * y verificar la lógica de negocio de forma aislada.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepo clienteRepo;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ClienteService clienteService;

    // =============================================
    // PRUEBA: Listar clientes devuelve lista de DTOs
    // =============================================
    @Test
    @DisplayName("Listar clientes devuelve una lista de ClienteDTO")
    void testListar_devuelveListaDeClientesDTO() {
        // Arrange - Preparamos 2 clientes simulados en la base de datos
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setUuid(UUID.randomUUID());
        cliente1.setNombre("Juan Pérez");
        cliente1.setCorreo("juan@test.com");
        cliente1.setTelefono("1234567890");
        cliente1.setEstatus(EstatusCliente.Activo);

        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setUuid(UUID.randomUUID());
        cliente2.setNombre("María López");
        cliente2.setCorreo("maria@test.com");
        cliente2.setTelefono("0987654321");
        cliente2.setEstatus(EstatusCliente.Activo);

        ClienteDTO dto1 = new ClienteDTO();
        dto1.setNombre("Juan Pérez");
        ClienteDTO dto2 = new ClienteDTO();
        dto2.setNombre("María López");

        // Simulamos que el repositorio devuelve nuestros 2 clientes
        when(clienteRepo.findAll()).thenReturn(Arrays.asList(cliente1, cliente2));
        // Simulamos que el mapper convierte cada entidad en un DTO
        when(mapper.map(cliente1, ClienteDTO.class)).thenReturn(dto1);
        when(mapper.map(cliente2, ClienteDTO.class)).thenReturn(dto2);

        // Act - Ejecutamos el método que queremos probar
        List<ClienteDTO> resultado = clienteService.listar();

        // Assert - Verificamos que el resultado es correcto
        assertEquals(2, resultado.size(), "Debe devolver exactamente 2 clientes");
        assertEquals("Juan Pérez", resultado.get(0).getNombre());
        assertEquals("María López", resultado.get(1).getNombre());
        verify(clienteRepo, times(1)).findAll();
    }

    // =============================================
    // PRUEBA: Guardar cliente correctamente
    // =============================================
    @Test
    @DisplayName("Guardar un cliente nuevo correctamente")
    void testGuardar_guardaClienteCorrectamente() {
        // Arrange - Creamos un DTO de entrada
        ClienteDTO dto = new ClienteDTO();
        dto.setNombre("Carlos García");
        dto.setCorreo("carlos@test.com");
        dto.setTelefono("5551234567");
        dto.setEstatus(EstatusCliente.Activo);

        Cliente entidad = new Cliente();
        entidad.setNombre("Carlos García");

        // Simulamos que el mapper convierte el DTO a entidad
        when(mapper.map(dto, Cliente.class)).thenReturn(entidad);

        // Act - Llamamos al método guardar
        clienteService.guardar(dto);

        // Assert - Verificamos que el repositorio guardó la entidad
        verify(clienteRepo, times(1)).save(entidad);
    }

    // =============================================
    // PRUEBA: Actualizar un cliente existente
    // =============================================
    @Test
    @DisplayName("Actualizar un cliente existente correctamente")
    void testActualiza_clienteExistente_actualizaCorrectamente() {
        // Arrange - El cliente ya existe en la BD
        UUID uuid = UUID.randomUUID();
        ClienteDTO dto = new ClienteDTO();
        dto.setUuid(uuid);
        dto.setNombre("Nombre Actualizado");
        dto.setCorreo("actualizado@test.com");
        dto.setTelefono("9999999999");

        Cliente existente = new Cliente();
        existente.setId(1L);
        existente.setUuid(uuid);
        existente.setNombre("Nombre Viejo");

        // Simulamos que el repositorio encuentra el cliente por UUID
        when(clienteRepo.findByUuid(uuid)).thenReturn(Optional.of(existente));

        // Act
        clienteService.actualiza(dto);

        // Assert - Verificamos que mapper.map fue llamado y luego save
        verify(mapper, times(1)).map(dto, existente);
        verify(clienteRepo, times(1)).save(existente);
    }

    // =============================================
    // PRUEBA: Actualizar un cliente que NO existe lanza excepción
    // =============================================
    @Test
    @DisplayName("Actualizar cliente no existente lanza EntityNotFoundException")
    void testActualiza_clienteNoExistente_lanzaExcepcion() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ClienteDTO dto = new ClienteDTO();
        dto.setUuid(uuid);

        // Simulamos que el repositorio NO encuentra el cliente
        when(clienteRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert - Verificamos que lanza la excepción esperada
        assertThrows(EntityNotFoundException.class, () -> clienteService.actualiza(dto),
            "Debe lanzar EntityNotFoundException cuando el cliente no existe");
    }

    // =============================================
    // PRUEBA: Obtener cliente por UUID cuando existe
    // =============================================
    @Test
    @DisplayName("Obtener cliente por UUID existente retorna DTO")
    void testObtenerClienteUUID_existente_retornaDTO() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        Cliente entidad = new Cliente();
        entidad.setUuid(uuid);
        entidad.setNombre("Test");

        ClienteDTO dtoEsperado = new ClienteDTO();
        dtoEsperado.setNombre("Test");

        when(clienteRepo.findByUuid(uuid)).thenReturn(Optional.of(entidad));
        when(mapper.map(entidad, ClienteDTO.class)).thenReturn(dtoEsperado);

        // Act
        ClienteDTO resultado = clienteService.obtenerClienteUUID(uuid);

        // Assert
        assertNotNull(resultado);
        assertEquals("Test", resultado.getNombre());
    }

    // =============================================
    // PRUEBA: Obtener cliente por UUID que no existe
    // =============================================
    @Test
    @DisplayName("Obtener cliente por UUID inexistente lanza excepción")
    void testObtenerClienteUUID_noExistente_lanzaExcepcion() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        when(clienteRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> clienteService.obtenerClienteUUID(uuid));
    }

    // =============================================
    // PRUEBA: Borrar cliente cambia estatus a Eliminado (soft delete)
    // =============================================
    @Test
    @DisplayName("Borrar cliente cambia su estatus a 'Eliminado' (soft delete)")
    void testBorrar_cambiaEstatusAEliminado() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setUuid(uuid);
        cliente.setEstatus(EstatusCliente.Activo);

        when(clienteRepo.findByUuid(uuid)).thenReturn(Optional.of(cliente));

        // Act
        clienteService.borrar(uuid);

        // Assert - Verificamos que el estatus cambió a Eliminado
        assertEquals(EstatusCliente.Eliminado, cliente.getEstatus(),
            "El estatus del cliente debe cambiar a 'Eliminado'");
        verify(clienteRepo, times(1)).save(cliente);
    }
}
