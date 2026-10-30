package Gimnasio.repository;

import Gimnasio.model.DiaRutina;
import Gimnasio.model.RutinaDia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RutinaDiaRepository extends JpaRepository<RutinaDia, Long> {

    List<RutinaDia> findBySocioIdSocio(Long idSocio);

    Optional<RutinaDia> findBySocioIdSocioAndDia(Long idSocio, DiaRutina dia);
}
