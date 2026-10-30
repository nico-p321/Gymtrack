package Gimnasio;

import Gimnasio.controller.AnalisisIaController;
import Gimnasio.model.Socio;
import Gimnasio.service.AnalisisIaService;
import Gimnasio.service.IaException;
import Gimnasio.service.IaNoConfiguradaException;
import Gimnasio.service.SocioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AnalisisIaController.
 * Verifica que el resultado o el error de la IA llegan al perfil como flash attributes
 * y que una falla de la IA nunca corta la navegación.
 */
class AnalisisIaControllerTest {

    @Mock
    private AnalisisIaService analisisIaService;

    @Mock
    private SocioService socioService;

    @InjectMocks
    private AnalisisIaController analisisIaController;

    private RedirectAttributesModelMap redirectAttributes;
    private Socio socio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redirectAttributes = new RedirectAttributesModelMap();
        socio = new Socio();
        socio.setIdSocio(1L);
        when(socioService.buscarPorId(1L)).thenReturn(Optional.of(socio));
    }

    /**
     * Verifica que el análisis exitoso se envía al perfil.
     */
    @Test
    void analizarProgreso_exitoso_muestraElAnalisis() {
        when(analisisIaService.analizarProgreso(socio)).thenReturn("Vas muy bien");

        String vista = analisisIaController.analizarProgreso(1L, redirectAttributes);

        assertEquals("redirect:/socios/1/pagos", vista);
        assertEquals("Vas muy bien", redirectAttributes.getFlashAttributes().get("analisisIa"));
        assertNull(redirectAttributes.getFlashAttributes().get("errorIa"));
    }

    /**
     * Verifica que si la IA no está configurada se avisa y se vuelve al perfil.
     */
    @Test
    void analizarProgreso_sinConfigurar_muestraAviso() {
        when(analisisIaService.analizarProgreso(socio)).thenThrow(new IaNoConfiguradaException());

        String vista = analisisIaController.analizarProgreso(1L, redirectAttributes);

        assertEquals("redirect:/socios/1/pagos", vista);
        assertTrue(((String) redirectAttributes.getFlashAttributes().get("errorIa")).contains("no está configurada"));
    }

    /**
     * Verifica que si la IA no responde se muestra el mensaje y se vuelve al perfil.
     */
    @Test
    void analizarProgreso_iaNoResponde_muestraMensajeClaro() {
        when(analisisIaService.analizarProgreso(socio))
                .thenThrow(new IaException("La IA no respondió a tiempo. Probá de nuevo en unos minutos."));

        String vista = analisisIaController.analizarProgreso(1L, redirectAttributes);

        assertEquals("redirect:/socios/1/pagos", vista);
        assertEquals("La IA no respondió a tiempo. Probá de nuevo en unos minutos.",
                redirectAttributes.getFlashAttributes().get("errorIa"));
    }

    /**
     * Verifica que sin datos para analizar se avisa qué cargar.
     */
    @Test
    void analizarProgreso_sinDatos_muestraAviso() {
        when(analisisIaService.analizarProgreso(socio))
                .thenThrow(new IllegalArgumentException("Cargá al menos una medida física o un entrenamiento"));

        analisisIaController.analizarProgreso(1L, redirectAttributes);

        assertNotNull(redirectAttributes.getFlashAttributes().get("errorIa"));
    }
}
