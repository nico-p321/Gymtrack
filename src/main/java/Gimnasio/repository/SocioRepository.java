package Gimnasio.repository;

import Gimnasio.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Socio.
 * Extiende JpaRepository para obtener operaciones CRUD básicas automáticamente.
 * Define consultas personalizadas para búsquedas frecuentes.
 */
public interface SocioRepository extends JpaRepository<Socio, Long> {
    
    // Busca un socio por email (único)
    Optional<Socio> findByEmail(String email);
    
    // Lista todos los socios activos
    List<Socio> findByActivoTrue();
    
    // Lista todos los socios inactivos (dados de baja)
    List<Socio> findByActivoFalse();
    
    // Búsqueda por texto en nombre o apellido (case insensitive)
    List<Socio> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}