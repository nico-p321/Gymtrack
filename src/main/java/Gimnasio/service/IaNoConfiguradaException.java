package Gimnasio.service;

/**
 * La variable de entorno con la clave de la IA no está definida.
 */
public class IaNoConfiguradaException extends IaException {

    public IaNoConfiguradaException() {
        super("La IA no está configurada. Definí la variable de entorno OPENROUTER_API_KEY y reiniciá la aplicación.");
    }
}
