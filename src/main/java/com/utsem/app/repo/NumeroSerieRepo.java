package com.utsem.app.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utsem.app.enums.EstadoUnidad;
import com.utsem.app.model.NumeroSerie;

@Repository
public interface NumeroSerieRepo extends JpaRepository<NumeroSerie, Long> {

	Optional<NumeroSerie> findByUuid(UUID uuid);

	List<NumeroSerie> findByDetProdId(Long detProdId);

	Optional<NumeroSerie> findByNumeroSerie(String numeroSerie);

	long countByDetProdIdAndEstadoUnidad(Long detProdId, EstadoUnidad estadoUnidad);

	List<NumeroSerie> findByDetProdIdAndEstadoUnidadOrderByFechaIngresoAsc(Long detProdId, EstadoUnidad estadoUnidad);

	List<NumeroSerie> findByPedidoId(Long pedidoId);
}
