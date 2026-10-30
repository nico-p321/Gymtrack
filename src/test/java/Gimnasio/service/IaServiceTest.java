package Gimnasio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Tests unitarios para IaService.
 * Usa MockRestServiceServer para simular las respuestas de la IA sin hacer llamadas reales.
 * Cubre los criterios de la HU: clave por variable de entorno, tolerancia a fallos y log sin datos.
 */
@ExtendWith(OutputCaptureExtension.class)
class IaServiceTest {

    private static final String URL = "https://ia.test/chat";
    private static final String PROMPT_SECRETO = "peso 80 kg dato-privado";

    private MockRestServiceServer servidor;

    private IaService crearServicio(String apiKey) {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        return new IaService(apiKey, URL, List.of("modelo-a", " modelo-b "), builder.build());
    }

    /**
     * Verifica que la clave se envía como Bearer, que se mandan los modelos de respaldo
     * y que se devuelve el texto de la respuesta.
     */
    @Test
    void consultar_conRespuestaValida_devuelveElTexto() {
        IaService ia = crearServicio("clave-test");
        servidor.expect(requestTo(URL))
                .andExpect(header("Authorization", "Bearer clave-test"))
                .andExpect(content().string(containsString("\"models\":[\"modelo-a\",\"modelo-b\"]")))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\" Buen progreso \"}}]}",
                        MediaType.APPLICATION_JSON));

        assertEquals("Buen progreso", ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));
        servidor.verify();
    }

    /**
     * Verifica que sin clave avisa que la IA no está configurada y no hace ninguna llamada.
     */
    @Test
    void consultar_sinClave_lanzaIaNoConfigurada() {
        IaService ia = crearServicio("  ");

        assertFalse(ia.estaConfigurada());
        IaException e = assertThrows(IaNoConfiguradaException.class,
                () -> ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));
        assertTrue(e.getMessage().contains("no está configurada"));
        servidor.verify();
    }

    /**
     * Verifica que si la IA tarda demasiado se lanza un error con mensaje claro.
     */
    @Test
    void consultar_conTimeout_lanzaMensajeClaro() {
        IaService ia = crearServicio("clave-test");
        servidor.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("timeout")));

        IaException e = assertThrows(IaException.class, () -> ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));
        assertTrue(e.getMessage().contains("no respondió a tiempo"));
    }

    /**
     * Verifica que un error del servidor de la IA se traduce a un mensaje claro.
     */
    @Test
    void consultar_conErrorDelServidor_lanzaMensajeClaro() {
        IaService ia = crearServicio("clave-test");
        servidor.expect(requestTo(URL)).andRespond(withServerError());

        IaException e = assertThrows(IaException.class, () -> ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));
        assertTrue(e.getMessage().contains("no está disponible"));
    }

    /**
     * Verifica que una clave rechazada por la IA se informa sin mostrar la clave.
     */
    @Test
    void consultar_conClaveInvalida_avisaSinMostrarLaClave() {
        IaService ia = crearServicio("clave-test");
        servidor.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        IaException e = assertThrows(IaException.class, () -> ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));
        assertTrue(e.getMessage().contains("no es válida"));
        assertFalse(e.getMessage().contains("clave-test"));
    }

    /**
     * Verifica que si el modelo configurado ya no existe se avisa qué revisar.
     */
    @Test
    void consultar_conModeloInexistente_avisaRevisarModelo() {
        IaService ia = crearServicio("clave-test");
        servidor.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        IaException e = assertThrows(IaException.class, () -> ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));
        assertTrue(e.getMessage().contains("ia.modelos"));
    }

    /**
     * Verifica que una respuesta sin contenido se informa como error.
     */
    @Test
    void consultar_conRespuestaVacia_lanzaError() {
        IaService ia = crearServicio("clave-test");
        servidor.expect(requestTo(URL)).andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));

        assertThrows(IaException.class, () -> ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));
    }

    /**
     * Verifica que el log registra fecha y tipo de análisis, pero no el prompt ni la clave.
     */
    @Test
    void consultar_registraEnLogSinDatosEnviados(CapturedOutput salida) {
        IaService ia = crearServicio("clave-test");
        servidor.expect(requestTo(URL)).andRespond(withServerError());

        assertThrows(IaException.class, () -> ia.consultar("PROGRESO_SOCIO", PROMPT_SECRETO));

        assertTrue(salida.getOut().contains("Consulta a IA - fecha:"));
        assertTrue(salida.getOut().contains("tipo de análisis: PROGRESO_SOCIO"));
        assertFalse(salida.getOut().contains("dato-privado"));
        assertFalse(salida.getOut().contains("clave-test"));
    }
}
