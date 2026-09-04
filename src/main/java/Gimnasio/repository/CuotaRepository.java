package Gimnasio.repository;

import Gimnasio.model.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {
    Optional<Cuota> findTopBySocioIdSocioOrderByFechaPagoDesc(Long idSocio);
    List<Cuota> findBySocioIdSocioOrderByFechaPagoDesc(Long idSocio);
}