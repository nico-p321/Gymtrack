package Gimnasio.repository;

import Gimnasio.model.MedidaFisica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedidaFisicaRepository extends JpaRepository<MedidaFisica, Long> {
    List<MedidaFisica> findBySocioIdSocioOrderByFechaDesc(Long idSocio);
}