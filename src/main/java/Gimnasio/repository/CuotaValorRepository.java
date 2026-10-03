package Gimnasio.repository;

import Gimnasio.model.CuotaValor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio para la entidad CuotaValor (historial de valores de cuota).
 * Permite obtener el valor vigente actual (el más reciente por fecha).
 */
public interface CuotaValorRepository extends JpaRepository<CuotaValor, Long> {
    
    // Obtiene el valor de cuota más reciente (vigente actual)
    Optional<CuotaValor> findTopByOrderByVigenteDesdeDesc();
}