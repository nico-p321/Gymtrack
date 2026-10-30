package Gimnasio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cliente de la IA (OpenRouter, API compatible con OpenAI).
 * - La clave se lee solo de la variable de entorno OPENROUTER_API_KEY, nunca del código ni de archivos versionados.
 * - Cualquier falla (sin clave, timeout, error HTTP) se traduce a una IaException con un mensaje claro,
 *   así el resto del sistema sigue funcionando.
 * - Cada consulta deja una línea en el log con fecha, tipo de análisis y resultado, sin los datos enviados.
 */
@Service
public class IaService {

    private static final Logger log = LoggerFactory.getLogger(IaService.class);

    private final String apiKey;
    private final String url;
    private final List<String> modelos;
    private final RestClient restClient;

    // ia.modelos: modelos de chat separados por coma. OpenRouter prueba el siguiente si uno no está disponible.
    // No se usa "openrouter/free" porque puede elegir modelos que no son de chat (ej: clasificadores de seguridad).
    @Autowired
    public IaService(@Value("${OPENROUTER_API_KEY:}") String apiKey,
                     @Value("${ia.url:https://openrouter.ai/api/v1/chat/completions}") String url,
                     @Value("${ia.modelos:nvidia/nemotron-3.5-lightning:free,google/gemma-4-31b-it:free,google/gemma-4-26b-a4b-it:free}") List<String> modelos,
                     @Value("${ia.timeout-segundos:30}") int timeoutSegundos) {
        this(apiKey, url, modelos, RestClient.builder()
                .requestFactory(crearRequestFactory(Duration.ofSeconds(timeoutSegundos)))
                .build());
    }

    // Permite inyectar un RestClient falso en los tests
    IaService(String apiKey, String url, List<String> modelos, RestClient restClient) {
        this.apiKey = apiKey;
        this.url = url;
        this.modelos = modelos.stream().map(String::trim).filter(m -> !m.isEmpty()).toList();
        this.restClient = restClient;
    }

    private static JdkClientHttpRequestFactory crearRequestFactory(Duration timeout) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(timeout);
        return factory;
    }

    public boolean estaConfigurada() {
        return apiKey != null && !apiKey.isBlank();
    }

    // Envía el prompt a la IA y devuelve el texto de la respuesta.
    // tipoAnalisis solo se usa para el log (ej: "PROGRESO_SOCIO").
    public String consultar(String tipoAnalisis, String prompt) {
        String resultado = "ERROR";
        try {
            if (!estaConfigurada()) {
                resultado = "NO_CONFIGURADA";
                throw new IaNoConfiguradaException();
            }

            Map<?, ?> respuesta = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", modelos.get(0),
                            "models", modelos,
                            "messages", List.of(Map.of("role", "user", "content", prompt))))
                    .retrieve()
                    .body(Map.class);

            String texto = extraerTexto(respuesta);
            resultado = "OK";
            return texto;

        } catch (ResourceAccessException e) {
            resultado = "SIN_RESPUESTA";
            throw new IaException("La IA no respondió a tiempo. Probá de nuevo en unos minutos.", e);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new IaException("La clave de la IA no es válida. Revisá la variable de entorno OPENROUTER_API_KEY.", e);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IaException("Los modelos de IA configurados ya no están disponibles. Revisá la propiedad ia.modelos.", e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new IaException("La IA está recibiendo demasiadas consultas. Probá de nuevo en unos minutos.", e);
        } catch (RestClientException e) {
            throw new IaException("La IA no está disponible en este momento. Probá de nuevo más tarde.", e);
        } finally {
            // No se registran el prompt ni la respuesta: solo fecha, tipo y resultado
            log.info("Consulta a IA - fecha: {}, tipo de análisis: {}, resultado: {}",
                    LocalDateTime.now(), tipoAnalisis, resultado);
        }
    }

    // Respuesta con formato OpenAI: { "choices": [ { "message": { "content": "..." } } ] }
    private String extraerTexto(Map<?, ?> respuesta) {
        if (respuesta != null
                && respuesta.get("choices") instanceof List<?> choices
                && !choices.isEmpty()
                && choices.get(0) instanceof Map<?, ?> choice
                && choice.get("message") instanceof Map<?, ?> mensaje
                && mensaje.get("content") instanceof String contenido
                && !contenido.isBlank()) {
            return contenido.trim();
        }
        throw new IaException("La IA devolvió una respuesta vacía. Probá de nuevo.");
    }
}
