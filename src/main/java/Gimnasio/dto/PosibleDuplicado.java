package Gimnasio.dto;

import Gimnasio.model.Socio;

import java.util.List;

/**
 * Socio ya registrado que coincide con el que se está cargando,
 * junto con los motivos de la coincidencia (ej: "mismo teléfono").
 */
public record PosibleDuplicado(Socio socio, List<String> motivos) {

    public String motivosTexto() {
        return String.join(" y ", motivos);
    }
}
