package Gimnasio.model;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo de objetivos que puede tener un socio dentro del gimnasio.
 * El nombre del enum es el código que se persiste en la columna "objetivo" de la tabla socios.
 * Un socio sin objetivo definido tiene la columna en NULL y se muestra como "Sin definir".
 */
public enum Objetivo {

    GANAR_MASA_MUSCULAR("Ganar masa muscular"),
    BAJAR_DE_PESO("Bajar de peso"),
    MANTENERSE("Mantenerse"),
    MEJORAR_RESISTENCIA("Mejorar resistencia");

    private static final String ETIQUETA_SIN_DEFINIR = "Sin definir";

    private final String etiqueta;

    Objetivo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    // Opciones para el <select> del perfil, en el orden en que se muestran
    public static List<Objetivo> opciones() {
        return Arrays.asList(values());
    }

    // Devuelve true si el código recibido corresponde a un objetivo del catálogo
    public static boolean esValido(String codigo) {
        return buscar(codigo) != null;
    }

    // Traduce el código persistido a su etiqueta; null/blank/desconocido -> "Sin definir"
    public static String etiquetaDe(String codigo) {
        Objetivo objetivo = buscar(codigo);
        return objetivo != null ? objetivo.etiqueta : ETIQUETA_SIN_DEFINIR;
    }

    private static Objetivo buscar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(o -> o.name().equals(codigo.trim()))
                .findFirst()
                .orElse(null);
    }
}