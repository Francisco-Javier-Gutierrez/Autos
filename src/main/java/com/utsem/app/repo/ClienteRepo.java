package com.utsem.app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.utsem.app.model.Cliente;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepo extends JpaRepository<Cliente, Long> {
	Optional<Cliente> findByUuid(UUID uuid);
}
