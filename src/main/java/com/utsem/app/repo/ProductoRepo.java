package com.utsem.app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.utsem.app.model.Producto;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepo extends JpaRepository<Producto, Long> {
	Optional<Producto> findByUuid(UUID uuid);
}