package Gimnasio.repository;

import Gimnasio.model.DiaRutina;
import Gimnasio.model.EjercicioRutina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EjercicioRutinaRepository extends JpaRepository<EjercicioRutina, Long> {

    // Ejercicios de la rutina del socio, en el orden en que se cargaron
    List<EjercicioRutina> findBySocioIdSocioOrderByIdEjercicioRutinaAsc(Long idSocio);

    List<EjercicioRutina> findBySocioIdSocioAndDiaOrderByIdEjercicioRutinaAsc(Long idSocio, DiaRutina dia);
}
