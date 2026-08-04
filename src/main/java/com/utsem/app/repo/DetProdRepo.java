package com.utsem.app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.utsem.app.model.DetProd;
import java.util.List;
import java.util.Optional;

@Repository
public interface DetProdRepo extends JpaRepository<DetProd, Long> {
	
	Optional<DetProd> findByUuid(java.util.UUID uuid);

	List<DetProd> findByProductoId(Long productoId);
	
}
