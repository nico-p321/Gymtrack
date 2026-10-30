package Gimnasio.dto;

import Gimnasio.model.DiaRutina;
import Gimnasio.model.EjercicioRutina;
import Gimnasio.model.GrupoMuscular;

import java.util.List;

/**
 * Un día de la rutina semanal tal como se muestra en pantalla:
 * los grupos musculares elegidos y los ejercicios planificados.
 */
public record DiaRutinaVista(DiaRutina dia, List<GrupoMuscular> grupos, List<EjercicioRutina> ejercicios) {

    // Sin grupos ni ejercicios se muestra como día de descanso
    public boolean esDescanso() {
        return grupos.isEmpty() && ejercicios.isEmpty();
    }

    // "Pecho · Hombro · Tríceps" para la pestaña del día
    public String resumenGrupos() {
        return String.join(" · ", grupos.stream().map(GrupoMuscular::getEtiqueta).toList());
    }

    public boolean tieneGrupo(GrupoMuscular grupo) {
        return grupos.contains(grupo);
    }
}
