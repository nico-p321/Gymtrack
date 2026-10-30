package Gimnasio.service;

/**
 * Error al consultar la IA. El mensaje está pensado para mostrarse tal cual al usuario,
 * por eso nunca incluye la clave ni los datos enviados.
 */
public class IaException extends RuntimeException {

    public IaException(String mensaje) {
        super(mensaje);
    }

    public IaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
