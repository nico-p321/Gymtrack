package Gimnasio.repository;

import Gimnasio.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocioRepository extends JpaRepository<Socio, Long> {
    Optional<Socio> findByEmail(String email);
    List<Socio> findByActivoTrue();
    List<Socio> findByActivoFalse();
    List<Socio> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}