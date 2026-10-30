package Gimnasio.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Catálogo de grupos musculares que el entrenador puede asignar a cada día de la rutina.
 * Se persisten como códigos separados por coma en RutinaDia.grupos.
 */
public enum GrupoMuscular {

    PECHO("Pecho"),
    ESPALDA("Espalda"),
    HOMBRO("Hombro"),
    BICEPS("Bíceps"),
    TRICEPS("Tríceps"),
    PIERNAS("Piernas"),
    GLUTEOS("Glúteos"),
    ABDOMEN("Abdomen"),
    CARDIO("Cardio");

    private final String etiqueta;

    GrupoMuscular(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static List<GrupoMuscular> opciones() {
        return Arrays.asList(values());
    }

    // Convierte "PECHO,HOMBRO" en la lista de grupos, ignorando códigos desconocidos
    public static List<GrupoMuscular> desdeCodigos(String codigos) {
        if (codigos == null || codigos.isBlank()) {
            return List.of();
        }
        return Arrays.stream(codigos.split(","))
                .map(String::trim)
                .map(c -> Arrays.stream(values()).filter(g -> g.name().equals(c)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}
