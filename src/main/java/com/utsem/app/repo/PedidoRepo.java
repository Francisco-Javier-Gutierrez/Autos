package com.utsem.app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.utsem.app.model.Pedido;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PedidoRepo extends JpaRepository<Pedido, Long> {
	Optional<Pedido> findByUuid(UUID uuid);
	
	void deleteByUuid(UUID uuid);
}
