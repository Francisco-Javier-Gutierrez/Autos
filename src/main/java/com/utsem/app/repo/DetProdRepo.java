package com.utsem.app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.utsem.app.model.DetProd;
import java.util.List;

@Repository
public interface DetProdRepo extends JpaRepository<DetProd, Long> {
	
	List<DetProd> findByProductoId(Long productoId);
	
}
