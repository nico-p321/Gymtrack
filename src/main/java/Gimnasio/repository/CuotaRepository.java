package Gimnasio.repository;

import Gimnasio.model.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Cuota (pagos).
 * Proporciona consultas específicas para obtener el historial de pagos de un socio.
 */
public interface CuotaRepository extends JpaRepository<Cuota, Long> {
    
    // Obtiene la última cuota pagada por un socio (la más reciente por fecha)
    Optional<Cuota> findTopBySocioIdSocioOrderByFechaPagoDesc(Long idSocio);
    
    // Obtiene todo el historial de pagos de un socio, ordenado de más reciente a más antiguo
    List<Cuota> findBySocioIdSocioOrderByFechaPagoDesc(Long idSocio);
}