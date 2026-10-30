package Gimnasio.service;

import Gimnasio.model.Entreno;
import Gimnasio.model.MedidaFisica;
import Gimnasio.model.Socio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Arma los análisis que se le piden a la IA a partir de los datos del socio.
 * Solo se envían datos de entrenamiento (objetivo, medidas y ejercicios):
 * nunca nombre, teléfono ni email.
 */
@Service
public class AnalisisIaService {

    static final String TIPO_PROGRESO = "PROGRESO_SOCIO";

    private static final int MAX_MEDIDAS = 10;
    private static final int MAX_ENTRENOS = 20;

    @Autowired
    private IaService iaService;

    @Autowired
    private MedidaFisicaService medidaFisicaService;

    @Autowired
    private EntrenoService entrenoService;

    // Pide a la IA un análisis del progreso del socio según su objetivo
    public String analizarProgreso(Socio socio) {
        List<MedidaFisica> medidas = medidaFisicaService.listarPorSocio(socio.getIdSocio());
        List<Entreno> entrenos = entrenoService.listarPorSocio(socio.getIdSocio());

        if (medidas.isEmpty() && entrenos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cargá al menos una medida física o un entrenamiento para poder analizar el progreso.");
        }

        String respuesta = iaService.consultar(TIPO_PROGRESO, armarPromptProgreso(socio, medidas, entrenos));
        return quitarMarkdown(respuesta);
    }


    String quitarMarkdown(String texto) {
        return texto.replace("**", "").replaceAll("(?m)^#+\\s*", "");
    }

    String armarPromptProgreso(Socio socio, List<MedidaFisica> medidas, List<Entreno> entrenos) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Sos un entrenador personal. Analizá el progreso de un socio de gimnasio ")
              .append("y dale recomendaciones concretas según su objetivo. ")
              .append("Respondé en español rioplatense, en texto plano sin formato Markdown, ")
              .append("en no más de 150 palabras.\n\n");

        prompt.append("Objetivo: ").append(socio.getObjetivoEtiqueta()).append("\n\n");

        prompt.append("Medidas físicas (más reciente primero):\n");
        if (medidas.isEmpty()) {
            prompt.append("- Sin medidas registradas\n");
        }
        medidas.stream().limit(MAX_MEDIDAS).forEach(m -> prompt
                .append("- ").append(m.getFecha())
                .append(": peso ").append(m.getPeso()).append(" kg")
                .append(", altura ").append(m.getAltura()).append(" cm")
                .append(m.getGrasaCorporal() != null ? ", grasa " + m.getGrasaCorporal() + " %" : "")
                .append("\n"));

        prompt.append("\nEntrenamientos (más reciente primero):\n");
        if (entrenos.isEmpty()) {
            prompt.append("- Sin entrenamientos registrados\n");
        }
        entrenos.stream().limit(MAX_ENTRENOS).forEach(e -> prompt
                .append("- ").append(e.getFecha())
                .append(": ").append(e.getEjercicio())
                .append(" ").append(e.getSeries()).append("x").append(e.getRepeticiones())
                .append(e.getPeso() != null ? " con " + e.getPeso() + " kg" : "")
                .append("\n"));

        return prompt.toString();
    }
}
