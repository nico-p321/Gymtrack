package Gimnasio.service;

import Gimnasio.dto.DiaRutinaVista;
import Gimnasio.model.DiaRutina;
import Gimnasio.model.EjercicioRutina;
import Gimnasio.model.GrupoMuscular;
import Gimnasio.model.RutinaDia;
import Gimnasio.model.Socio;
import Gimnasio.repository.EjercicioRutinaRepository;
import Gimnasio.repository.RutinaDiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rutina semanal del socio: el entrenador elige para cada día (lunes a sábado)
 * los grupos musculares y los ejercicios. La rutina se puede pasar al historial
 * de entrenamientos cuando el socio la realiza.
 */
@Service
public class RutinaService {

    @Autowired
    private RutinaDiaRepository rutinaDiaRepository;

    @Autowired
    private EjercicioRutinaRepository ejercicioRutinaRepository;

    @Autowired
    private EntrenoService entrenoService;

    // Los seis días en orden, con sus grupos y ejercicios (vacíos si no se cargó nada)
    public List<DiaRutinaVista> obtenerSemana(Long idSocio) {
        Map<DiaRutina, RutinaDia> grupos = rutinaDiaRepository.findBySocioIdSocio(idSocio).stream()
                .collect(Collectors.toMap(RutinaDia::getDia, Function.identity()));
        Map<DiaRutina, List<EjercicioRutina>> ejercicios =
                ejercicioRutinaRepository.findBySocioIdSocioOrderByIdEjercicioRutinaAsc(idSocio).stream()
                        .collect(Collectors.groupingBy(EjercicioRutina::getDia));

        return Arrays.stream(DiaRutina.values())
                .map(dia -> new DiaRutinaVista(
                        dia,
                        grupos.containsKey(dia) ? GrupoMuscular.desdeCodigos(grupos.get(dia).getGrupos()) : List.of(),
                        ejercicios.getOrDefault(dia, List.of())))
                .toList();
    }

    // Reemplaza los grupos musculares del día; una lista vacía deja el día sin grupos
    public void guardarGrupos(Socio socio, DiaRutina dia, List<String> codigos) {
        String grupos = (codigos == null ? List.<String>of() : codigos).stream()
                .map(String::trim)
                .filter(c -> Arrays.stream(GrupoMuscular.values()).anyMatch(g -> g.name().equals(c)))
                .distinct()
                .collect(Collectors.joining(","));

        RutinaDia rutinaDia = rutinaDiaRepository.findBySocioIdSocioAndDia(socio.getIdSocio(), dia)
                .orElseGet(() -> {
                    RutinaDia nuevo = new RutinaDia();
                    nuevo.setSocio(socio);
                    nuevo.setDia(dia);
                    return nuevo;
                });
        rutinaDia.setGrupos(grupos);
        rutinaDiaRepository.save(rutinaDia);
    }

    public EjercicioRutina agregarEjercicio(Socio socio, DiaRutina dia, String ejercicio,
                                            Integer series, Integer repeticiones, BigDecimal peso) {
        EntrenoService.validarEjercicio(ejercicio, series, repeticiones, peso);

        EjercicioRutina nuevo = new EjercicioRutina();
        nuevo.setSocio(socio);
        nuevo.setDia(dia);
        nuevo.setEjercicio(ejercicio.trim());
        nuevo.setSeries(series);
        nuevo.setRepeticiones(repeticiones);
        nuevo.setPeso(peso);
        return ejercicioRutinaRepository.save(nuevo);
    }

    // Verifica que el ejercicio pertenezca al socio para que no se pueda borrar otro por URL
    public void eliminarEjercicio(Long idSocio, Long idEjercicioRutina) {
        EjercicioRutina ejercicio = ejercicioRutinaRepository.findById(idEjercicioRutina)
                .orElseThrow(() -> new RuntimeException("El ejercicio no existe"));

        if (!ejercicio.getSocio().getIdSocio().equals(idSocio)) {
            throw new RuntimeException("Ese ejercicio no pertenece a este socio");
        }

        ejercicioRutinaRepository.delete(ejercicio);
    }

    // Copia los ejercicios del día al historial con la fecha indicada; devuelve cuántos registró
    public int registrarComoRealizada(Socio socio, DiaRutina dia, LocalDate fecha) {
        List<EjercicioRutina> ejercicios =
                ejercicioRutinaRepository.findBySocioIdSocioAndDiaOrderByIdEjercicioRutinaAsc(socio.getIdSocio(), dia);

        if (ejercicios.isEmpty()) {
            throw new IllegalArgumentException("El " + dia.getEtiqueta().toLowerCase() + " no tiene ejercicios cargados");
        }

        for (EjercicioRutina e : ejercicios) {
            entrenoService.registrar(socio, fecha, e.getEjercicio(), e.getSeries(), e.getRepeticiones(), e.getPeso());
        }
        return ejercicios.size();
    }
}
