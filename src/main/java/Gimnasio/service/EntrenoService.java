package Gimnasio.service;

import Gimnasio.model.Entreno;
import Gimnasio.model.Socio;
import Gimnasio.repository.EntrenoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que encapsula la lógica de negocio del historial de entrenamientos.
 * Valida los datos cargados y los asocia al socio correspondiente.
 */
@Service
public class EntrenoService {

    @Autowired
    private EntrenoRepository entrenoRepository;

    // Registra un ejercicio para el socio. Si no se pasa fecha, usa la de hoy.
    // Lanza IllegalArgumentException con un mensaje claro si los datos no son válidos.
    public Entreno registrar(Socio socio, LocalDate fecha, String ejercicio,
                             Integer series, Integer repeticiones, BigDecimal peso) {

        validarEjercicio(ejercicio, series, repeticiones, peso);

        Entreno entreno = new Entreno();
        entreno.setSocio(socio);
        entreno.setFecha(fecha != null ? fecha : LocalDate.now());
        entreno.setEjercicio(ejercicio.trim());
        entreno.setSeries(series);
        entreno.setRepeticiones(repeticiones);
        entreno.setPeso(peso);

        return entrenoRepository.save(entreno);
    }

    // Validación compartida con la rutina semanal (RutinaService)
    public static void validarEjercicio(String ejercicio, Integer series, Integer repeticiones, BigDecimal peso) {
        if (ejercicio == null || ejercicio.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el nombre del ejercicio");
        }
        if (series == null || series <= 0) {
            throw new IllegalArgumentException("Las series deben ser mayores a cero");
        }
        if (repeticiones == null || repeticiones <= 0) {
            throw new IllegalArgumentException("Las repeticiones deben ser mayores a cero");
        }
        if (peso != null && peso.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo");
        }
    }

    // Historial de entrenamientos de un socio, más reciente primero
    public List<Entreno> listarPorSocio(Long idSocio) {
        return entrenoRepository.findBySocioIdSocioOrderByFechaDescIdEntrenoDesc(idSocio);
    }

    // Elimina un registro del historial. Verifica que el registro pertenezca al socio
    // para que nadie pueda borrar entrenamientos de otro socio por URL.
    public void eliminar(Long idSocio, Long idEntreno) {
        Entreno entreno = entrenoRepository.findById(idEntreno)
                .orElseThrow(() -> new RuntimeException("El entrenamiento no existe"));

        if (!entreno.getSocio().getIdSocio().equals(idSocio)) {
            throw new RuntimeException("Ese entrenamiento no pertenece a este socio");
        }

        entrenoRepository.delete(entreno);
    }
}