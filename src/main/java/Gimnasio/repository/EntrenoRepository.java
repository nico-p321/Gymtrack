package Gimnasio.repository;

import Gimnasio.model.Entreno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntrenoRepository extends JpaRepository<Entreno, Long> {

    // Historial del socio, del ejercicio más reciente al más antiguo
    List<Entreno> findBySocioIdSocioOrderByFechaDescIdEntrenoDesc(Long idSocio);
}