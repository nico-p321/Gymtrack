package Gimnasio;


import Gimnasio.controller.CuotaController;
import Gimnasio.model.Socio;
import Gimnasio.service.CuotaService;
import Gimnasio.service.SocioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CuotaController.
 * Prueba la capa de controlador MVC mockeando los servicios.
 * Usa ExtendedModelMap (implementación de Model) para capturar atributos agregados al modelo.
 */
class CuotaControllerTest {

    // Mocks de los servicios que usa el controlador
    @Mock
    private CuotaService cuotaService;

    @Mock
    private SocioService socioService;

    // Inyecta los mocks en el controlador bajo prueba
    @InjectMocks
    private CuotaController cuotaController;

    // Model real (ExtendedModelMap) para verificar qué atributos agrega el controlador
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ExtendedModelMap();
    }

    /**
     * Verifica que verPagos() agrega la fecha de hoy al modelo con clave "hoy".
     * Configura los mocks para devolver valores simulados cuando el controlador llama a los servicios.
     */
    @Test
    void verPagos_incluyeLaFechaDeHoyEnElModelo() {
        // Arrange: crea un socio simulado y configura los mocks
        Socio socio = new Socio();
        socio.setIdSocio(1L);

        // Cuando el controlador llame a socioService.buscarPorId(1L), devuelve el socio simulado
        when(socioService.buscarPorId(1L)).thenReturn(Optional.of(socio));
        // Cuando llame a historialDePagos, devuelve lista vacía
        when(cuotaService.historialDePagos(1L)).thenReturn(List.of());
        // Cuando llame a calcularEstado, devuelve este string
        when(cuotaService.calcularEstado(1L)).thenReturn("SIN PAGOS REGISTRADOS");

        // Act: ejecuta el método del controlador
        cuotaController.verPagos(1L, model);

        // Assert: verifica que el modelo contiene "hoy" con la fecha actual
        assertEquals(LocalDate.now(), model.getAttribute("hoy"));
    }
}
