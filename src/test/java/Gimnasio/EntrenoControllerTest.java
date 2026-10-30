package Gimnasio;

import Gimnasio.controller.EntrenoController;
import Gimnasio.dto.DiaEntrenado;
import Gimnasio.dto.DiaRutinaVista;
import Gimnasio.model.DiaRutina;
import Gimnasio.model.Entreno;
import Gimnasio.model.Socio;
import Gimnasio.service.EntrenoService;
import Gimnasio.service.RutinaService;
import Gimnasio.service.SocioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EntrenoController.
 * Prueba la capa de controlador mockeando los servicios y capturando
 * los atributos del modelo y los flash attributes de la redirección.
 */
class EntrenoControllerTest {

    @Mock
    private EntrenoService entrenoService;

    @Mock
    private SocioService socioService;

    @Mock
    private RutinaService rutinaService;

    @InjectMocks
    private EntrenoController entrenoController;

    private Model model;
    private RedirectAttributesModelMap redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ExtendedModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    private Socio crearSocio() {
        Socio socio = new Socio();
        socio.setIdSocio(1L);
        socio.setNombre("Carlos");
        socio.setApellido("Perez");
        return socio;
    }

    /**
     * Verifica que verEntrenamientos() carga el historial del socio y la fecha de hoy en el modelo.
     */
    @Test
    void verEntrenamientos_cargaElHistorialDelSocio() {
        // Arrange
        Socio socio = crearSocio();
        Entreno entreno = new Entreno();
        entreno.setIdEntreno(7L);
        entreno.setEjercicio("Press de banca");
        entreno.setFecha(LocalDate.of(2026, 10, 20));

        when(socioService.buscarPorId(1L)).thenReturn(Optional.of(socio));
        when(entrenoService.listarPorSocio(1L)).thenReturn(List.of(entreno));
        when(rutinaService.obtenerSemana(1L)).thenReturn(semanaVacia());

        // Act
        String vista = entrenoController.verEntrenamientos(1L, null, model);

        // Assert
        assertEquals("socio-entrenos", vista);
        assertEquals(socio, model.getAttribute("socio"));
        assertEquals(LocalDate.now(), model.getAttribute("hoy"));
        assertEquals(List.of(entreno), model.getAttribute("entrenos"));
        assertEquals(List.of(new DiaEntrenado(LocalDate.of(2026, 10, 20), List.of(entreno))),
                model.getAttribute("historial"));
        assertEquals(DiaRutina.deFecha(LocalDate.now()), ((DiaRutinaVista) model.getAttribute("diaElegido")).dia());
    }

    /**
     * Verifica que el día pedido por parámetro queda seleccionado en la rutina semanal.
     */
    @Test
    void verEntrenamientos_conDia_seleccionaEseDia() {
        when(socioService.buscarPorId(1L)).thenReturn(Optional.of(crearSocio()));
        when(entrenoService.listarPorSocio(1L)).thenReturn(List.of());
        when(rutinaService.obtenerSemana(1L)).thenReturn(semanaVacia());

        entrenoController.verEntrenamientos(1L, "martes", model);

        assertEquals(DiaRutina.MARTES, ((DiaRutinaVista) model.getAttribute("diaElegido")).dia());
    }

    private List<DiaRutinaVista> semanaVacia() {
        return Arrays.stream(DiaRutina.values())
                .map(d -> new DiaRutinaVista(d, List.of(), List.of()))
                .toList();
    }

    /**
     * Verifica que guardar() registra el ejercicio y redirige al historial del socio.
     */
    @Test
    void guardar_registraElEjercicioYRedirige() {
        // Arrange
        when(socioService.buscarPorId(1L)).thenReturn(Optional.of(crearSocio()));

        // Act
        String resultado = entrenoController.guardar(1L, LocalDate.of(2026, 10, 1), "Press de banca",
                4, 8, new BigDecimal("80"), redirectAttributes);

        // Assert
        verify(entrenoService).registrar(any(Socio.class), eq(LocalDate.of(2026, 10, 1)),
                eq("Press de banca"), eq(4), eq(8), eq(new BigDecimal("80")));
        assertEquals("redirect:/socios/1/entrenos", resultado);
        assertEquals("Entrenamiento registrado correctamente", redirectAttributes.getFlashAttributes().get("exitoEntreno"));
    }

    /**
     * Verifica que guardar() con series en cero no registra nada y devuelve el error en un flash attribute.
     */
    @Test
    void guardar_conSeriesEnCero_muestraElError() {
        // Arrange
        when(socioService.buscarPorId(1L)).thenReturn(Optional.of(crearSocio()));

        // Act: el servicio lanza IllegalArgumentException al validar
        doThrow(new IllegalArgumentException("Las series deben ser mayores a cero"))
                .when(entrenoService).registrar(any(), any(), any(), any(), any(), any());

        String resultado = entrenoController.guardar(1L, LocalDate.now(), "Press de banca",
                0, 8, null, redirectAttributes);

        // Assert
        assertEquals("redirect:/socios/1/entrenos", resultado);
        assertEquals("Las series deben ser mayores a cero", redirectAttributes.getFlashAttributes().get("errorEntreno"));
    }

    /**
     * Verifica que eliminar() borra el registro y redirige al historial del socio.
     */
    @Test
    void eliminar_borraElRegistroYRedirige() {
        // Act
        String resultado = entrenoController.eliminar(1L, 7L, redirectAttributes);

        // Assert
        verify(entrenoService).eliminar(1L, 7L);
        assertEquals("redirect:/socios/1/entrenos", resultado);
        assertEquals("Entrenamiento eliminado", redirectAttributes.getFlashAttributes().get("exitoEntreno"));
    }

    /**
     * Verifica que si no se puede eliminar el registro, se informa el motivo sin romper la navegación.
     */
    @Test
    void eliminar_conError_muestraElMensajeEnFlash() {
        // Arrange
        doThrow(new RuntimeException("Ese entrenamiento no pertenece a este socio"))
                .when(entrenoService).eliminar(1L, 7L);

        // Act
        String resultado = entrenoController.eliminar(1L, 7L, redirectAttributes);

        // Assert
        assertEquals("redirect:/socios/1/entrenos", resultado);
        assertEquals("Ese entrenamiento no pertenece a este socio",
                redirectAttributes.getFlashAttributes().get("errorEntreno"));
    }
}