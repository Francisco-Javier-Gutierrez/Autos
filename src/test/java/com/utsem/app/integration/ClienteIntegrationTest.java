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
import com.utsem.app.dto.ClienteDTO;
import com.utsem.app.enums.EstatusCliente;
import com.utsem.app.model.Cliente;
import com.utsem.app.repo.ClienteRepo;
import com.utsem.app.seeder.DatabaseSeeder;
import com.utsem.app.service.ClienteService;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ClienteIntegrationTest {

    @MockitoBean
    private DatabaseConfig databaseConfig;

    @MockitoBean
    private DatabaseSeeder databaseSeeder;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepo clienteRepo;

    @BeforeEach
    void setUp() {
        // Limpiamos el repositorio antes de cada test
        clienteRepo.deleteAll();
    }

    @Test
    @DisplayName("Guardar y listar cliente")
    void testGuardarYListarCliente() {
        // Arrange
        ClienteDTO dto = new ClienteDTO();
        dto.setNombre("Juan Perez");
        dto.setCorreo("juan@test.com");
        dto.setTelefono("1234567890");
        dto.setEstatus(EstatusCliente.Activo);
        
        // Act
        clienteService.guardar(dto);
        List<ClienteDTO> clientes = clienteService.listar();
        
        // Assert
        assertThat(clientes).hasSize(1);
        assertThat(clientes.get(0).getNombre()).isEqualTo("Juan Perez");
    }

    @Test
    @DisplayName("Actualizar cliente existente")
    void testActualizarCliente() {
        // Arrange
        ClienteDTO dto = new ClienteDTO();
        dto.setNombre("Pedro");
        dto.setCorreo("pedro@test.com");
        dto.setTelefono("1234567890");
        dto.setEstatus(EstatusCliente.Activo);
        clienteService.guardar(dto);
        
        List<ClienteDTO> clientes = clienteService.listar();
        ClienteDTO guardado = clientes.get(0);
        
        // Act
        guardado.setNombre("Pedro Actualizado");
        clienteService.actualiza(guardado);
        
        // Assert
        ClienteDTO actualizado = clienteService.obtenerClienteUUID(guardado.getUuid());
        assertThat(actualizado.getNombre()).isEqualTo("Pedro Actualizado");
    }

    @Test
    @DisplayName("Borrar cliente cambia estatus a Eliminado")
    void testBorrarCliente_cambiaEstatusAEliminado() {
        // Arrange
        ClienteDTO dto = new ClienteDTO();
        dto.setNombre("Maria");
        dto.setCorreo("maria@test.com");
        dto.setTelefono("1234567890");
        dto.setEstatus(EstatusCliente.Activo);
        clienteService.guardar(dto);
        
        List<ClienteDTO> clientes = clienteService.listar();
        UUID uuid = clientes.get(0).getUuid();
        
        // Act
        clienteService.borrar(uuid);
        
        // Assert
        Cliente cliente = clienteRepo.findByUuid(uuid).orElseThrow();
        assertThat(cliente.getEstatus()).isEqualTo(EstatusCliente.Eliminado);
    }

    @Test
    @DisplayName("Obtener cliente con UUID inexistente lanza excepcion")
    void testObtenerClientePorUUID_noExistente_lanzaExcepcion() {
        // Arrange
        UUID random = UUID.randomUUID();
        
        // Act & Assert
        assertThatThrownBy(() -> clienteService.obtenerClienteUUID(random))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Guardar multiples clientes")
    void testGuardarMultiplesClientes_listarDevuelveTodos() {
        // Arrange
        for(int i = 0; i < 3; i++) {
            ClienteDTO dto = new ClienteDTO();
            dto.setNombre("Cliente " + i);
            dto.setCorreo("cliente" + i + "@test.com");
            dto.setTelefono("1234567890");
            dto.setEstatus(EstatusCliente.Activo);
            clienteService.guardar(dto);
        }
        
        // Act
        List<ClienteDTO> clientes = clienteService.listar();
        
        // Assert
        assertThat(clientes).hasSize(3);
    }
}
