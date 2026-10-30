package Gimnasio;

import Gimnasio.controller.SocioController;
import Gimnasio.dto.PosibleDuplicado;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el guardado de socios en SocioController:
 * email repetido, aviso de posibles duplicados y confirmación del usuario.
 */
class SocioGuardarControllerTest {

    @Mock
    private SocioService socioService;

    @Mock
    private CuotaService cuotaService;

    @InjectMocks
    private SocioController socioController;

    private Model model;
    private Socio socio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ExtendedModelMap();
        socio = new Socio();
        socio.setNombre("Ana");
        socio.setApellido("Gómez");
    }

    /**
     * Verifica que sin coincidencias el socio se guarda y vuelve al listado.
     */
    @Test
    void guardar_sinDuplicados_guardaYRedirige() {
        when(socioService.buscarPosiblesDuplicados(socio)).thenReturn(List.of());

        String vista = socioController.guardar(socio, false, model);

        assertEquals("redirect:/socios", vista);
        verify(socioService).guardarConValidacion(socio);
    }

    /**
     * Verifica que con email repetido se muestra el error y no se guarda.
     */
    @Test
    void guardar_emailRepetido_muestraErrorYNoGuarda() {
        when(socioService.validarEmail(socio)).thenReturn("Ese email ya pertenece a otro socio");

        String vista = socioController.guardar(socio, true, model);

        assertEquals("socio-form", vista);
        assertEquals("Ese email ya pertenece a otro socio", model.getAttribute("error"));
        assertNotNull(model.getAttribute("objetivos"));
        verify(socioService, never()).guardarConValidacion(any());
    }

    /**
     * Verifica que si hay posibles duplicados se avisa y no se guarda hasta confirmar.
     */
    @Test
    void guardar_conPosiblesDuplicados_avisaYNoGuarda() {
        Socio existente = new Socio();
        existente.setIdSocio(1L);
        List<PosibleDuplicado> duplicados = List.of(new PosibleDuplicado(existente, List.of("mismo teléfono")));
        when(socioService.buscarPosiblesDuplicados(socio)).thenReturn(duplicados);

        String vista = socioController.guardar(socio, false, model);

        assertEquals("socio-form", vista);
        assertEquals(duplicados, model.getAttribute("posiblesDuplicados"));
        assertEquals(socio, model.getAttribute("socio"));
        verify(socioService, never()).guardarConValidacion(any());
    }

    /**
     * Verifica que si el usuario confirma "Guardar igual" se guarda sin volver a avisar.
     */
    @Test
    void guardar_confirmado_guardaAunqueHayaDuplicados() {
        String vista = socioController.guardar(socio, true, model);

        assertEquals("redirect:/socios", vista);
        verify(socioService, never()).buscarPosiblesDuplicados(any());
        verify(socioService).guardarConValidacion(socio);
    }
}
