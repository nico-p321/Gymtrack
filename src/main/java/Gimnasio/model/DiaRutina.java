package Gimnasio.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Días de la semana en los que se puede armar una rutina (domingo no se entrena).
 * El nombre del enum es el código que se persiste en la base de datos.
 */
public enum DiaRutina {

    LUNES("Lunes", DayOfWeek.MONDAY),
    MARTES("Martes", DayOfWeek.TUESDAY),
    MIERCOLES("Miércoles", DayOfWeek.WEDNESDAY),
    JUEVES("Jueves", DayOfWeek.THURSDAY),
    VIERNES("Viernes", DayOfWeek.FRIDAY),
    SABADO("Sábado", DayOfWeek.SATURDAY);

    private final String etiqueta;
    private final DayOfWeek diaSemana;

    DiaRutina(String etiqueta, DayOfWeek diaSemana) {
        this.etiqueta = etiqueta;
        this.diaSemana = diaSemana;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    // Día de rutina que corresponde a una fecha; el domingo se muestra como lunes
    public static DiaRutina deFecha(LocalDate fecha) {
        return Arrays.stream(values())
                .filter(d -> d.diaSemana == fecha.getDayOfWeek())
                .findFirst()
                .orElse(LUNES);
    }

    // Traduce el código recibido por URL; null/desconocido -> null
    public static DiaRutina buscar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(d -> d.name().equalsIgnoreCase(codigo.trim()))
                .findFirst()
                .orElse(null);
    }
}
