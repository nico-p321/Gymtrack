package Gimnasio.dto;

import Gimnasio.model.Entreno;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ejercicios registrados en una misma fecha, para mostrar el historial agrupado por día.
 */
public record DiaEntrenado(LocalDate fecha, List<Entreno> ejercicios) {

    private static final Locale ES = Locale.of("es", "AR");

    // "Lunes" a partir de la fecha
    public String nombreDia() {
        String nombre = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, ES);
        return nombre.substring(0, 1).toUpperCase(ES) + nombre.substring(1);
    }

    // "1 ejercicio" / "3 ejercicios"
    public String cantidadEjercicios() {
        return ejercicios.size() + (ejercicios.size() == 1 ? " ejercicio" : " ejercicios");
    }

    // Agrupa el historial (ya ordenado por fecha descendente) respetando ese orden de fechas;
    // dentro de cada fecha los ejercicios quedan en el orden en que se cargaron
    public static List<DiaEntrenado> agrupar(List<Entreno> entrenos) {
        Map<LocalDate, List<Entreno>> porFecha = new LinkedHashMap<>();
        for (Entreno entreno : entrenos) {
            porFecha.computeIfAbsent(entreno.getFecha(), f -> new ArrayList<>()).add(entreno);
        }
        porFecha.values().forEach(lista ->
                lista.sort(Comparator.comparing(Entreno::getIdEntreno, Comparator.nullsLast(Comparator.naturalOrder()))));
        return porFecha.entrySet().stream()
                .map(e -> new DiaEntrenado(e.getKey(), e.getValue()))
                .toList();
    }
}
