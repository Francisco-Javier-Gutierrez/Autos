package com.utsem.app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // IMPORTANTE: Importación de Spring

import com.utsem.app.model.Color;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ColorRepo extends JpaRepository<Color, Long> {
	Optional<Color> findByUuid(UUID uuid);
		@Transactional
	void deleteByUuid(UUID uuid);
}