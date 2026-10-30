package Gimnasio;

import Gimnasio.model.Socio;
import Gimnasio.repository.SocioRepository;
import Gimnasio.service.SocioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para la gestión del objetivo del socio en SocioService.
 * Usa ArgumentCaptor para verificar qué socio se guardó y con qué valor.
 */
class SocioServiceTest {

    @Mock
    private SocioRepository socioRepository;

    @InjectMocks
    private SocioService socioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Socio crearSocio(Long id) {
        Socio s = new Socio();
        s.setIdSocio(id);
        s.setNombre("Test");
        s.setApellido("Socio" + id);
        s.setActivo(true);
        return s;
    }

    /**
     * Verifica que al elegir un objetivo de la lista queda guardado en el socio.
     */
    @Test
    void actualizarObjetivo_seleccionaUno_loGuardaEnElSocio() {
        Socio socio = crearSocio(1L);
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));

        socioService.actualizarObjetivo(1L, "BAJAR_DE_PESO");

        ArgumentCaptor<Socio> guardados = ArgumentCaptor.forClass(Socio.class);
        verify(socioRepository).save(guardados.capture());
        assertEquals("BAJAR_DE_PESO", guardados.getValue().getObjetivo());
    }

    /**
     * Verifica que cambiar el objetivo de un socio que ya tenía uno
     * actualiza el valor vigente (no lo acumula ni lo ignora).
     */
    @Test
    void actualizarObjetivo_cambiaElValorVigente() {
        Socio socio = crearSocio(2L);
        socio.setObjetivo("GANAR_MASA_MUSCULAR");
        when(socioRepository.findById(2L)).thenReturn(Optional.of(socio));

        socioService.actualizarObjetivo(2L, "MEJORAR_RESISTENCIA");

        assertEquals("MEJORAR_RESISTENCIA", socio.getObjetivo());
        verify(socioRepository).save(socio);
    }

    /**
     * Verifica que un valor vacío deja el objetivo sin definir (columna en null).
     */
    @Test
    void actualizarObjetivo_valorVacio_dejaElObjetivoEnNull() {
        Socio socio = crearSocio(3L);
        socio.setObjetivo("MANTENERSE");
        when(socioRepository.findById(3L)).thenReturn(Optional.of(socio));

        socioService.actualizarObjetivo(3L, "  ");

        assertNull(socio.getObjetivo());
        verify(socioRepository).save(socio);
    }

    /**
     * Verifica que un código fuera del catálogo se rechaza sin tocar el valor vigente.
     */
    @Test
    void actualizarObjetivo_codigoInvalido_lanzaErrorYNoGuarda() {
        Socio socio = crearSocio(4L);
        when(socioRepository.findById(4L)).thenReturn(Optional.of(socio));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> socioService.actualizarObjetivo(4L, "PERDER_MASA_MUSI"));

        assertEquals("El objetivo seleccionado no es válido", error.getMessage());
        verify(socioRepository, never()).save(any(Socio.class));
    }
}