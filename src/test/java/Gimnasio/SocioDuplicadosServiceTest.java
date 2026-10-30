package Gimnasio;

import Gimnasio.dto.PosibleDuplicado;
import Gimnasio.model.Socio;
import Gimnasio.repository.SocioRepository;
import Gimnasio.service.SocioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el control de socios duplicados en SocioService:
 * email repetido, mismo nombre y apellido, mismo teléfono y edición de socios.
 */
class SocioDuplicadosServiceTest {

    @Mock
    private SocioRepository socioRepository;

    @InjectMocks
    private SocioService socioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Socio socio(Long id, String nombre, String apellido, String telefono, String email) {
        Socio s = new Socio();
        s.setIdSocio(id);
        s.setNombre(nombre);
        s.setApellido(apellido);
        s.setTelefono(telefono);
        s.setEmail(email);
        return s;
    }

    /**
     * Verifica que un email ya usado por otro socio se rechaza, sin importar mayúsculas ni espacios.
     */
    @Test
    void validarEmail_repetidoConOtrasMayusculas_devuelveError() {
        when(socioRepository.findAll()).thenReturn(List.of(socio(1L, "Ana", "Gómez", null, "ana@mail.com")));

        String error = socioService.validarEmail(socio(null, "Otra", "Persona", null, "  ANA@Mail.com "));

        assertEquals("Ese email ya pertenece a otro socio", error);
    }

    /**
     * Verifica que al editar un socio sin cambiar su email no se lo marca como repetido.
     */
    @Test
    void validarEmail_mismoSocioEditado_noDevuelveError() {
        when(socioRepository.findAll()).thenReturn(List.of(socio(1L, "Ana", "Gómez", null, "ana@mail.com")));

        assertNull(socioService.validarEmail(socio(1L, "Ana", "Gómez", null, "ana@mail.com")));
    }

    /**
     * Verifica que varios socios pueden no tener email.
     */
    @Test
    void validarEmail_vacio_noDevuelveError() {
        when(socioRepository.findAll()).thenReturn(List.of(socio(1L, "Ana", "Gómez", null, null)));

        assertNull(socioService.validarEmail(socio(null, "Juan", "Pérez", null, "  ")));
    }

    /**
     * Verifica que guardar con email repetido no guarda nada.
     */
    @Test
    void guardarConValidacion_emailRepetido_noGuarda() {
        when(socioRepository.findAll()).thenReturn(List.of(socio(1L, "Ana", "Gómez", null, "ana@mail.com")));

        String error = socioService.guardarConValidacion(socio(null, "Otra", "Persona", null, "ana@mail.com"));

        assertNotNull(error);
        verify(socioRepository, never()).save(any());
    }

    /**
     * Verifica que el mismo nombre y apellido se detecta sin importar mayúsculas, tildes ni espacios.
     */
    @Test
    void buscarPosiblesDuplicados_mismoNombreYApellido() {
        Socio existente = socio(1L, "José", "Pérez", "1111", "jose@mail.com");
        when(socioRepository.findAll()).thenReturn(List.of(existente));

        List<PosibleDuplicado> duplicados =
                socioService.buscarPosiblesDuplicados(socio(null, "  jose ", "PEREZ", "2222", null));

        assertEquals(1, duplicados.size());
        assertEquals(existente, duplicados.get(0).socio());
        assertEquals(List.of("mismo nombre y apellido"), duplicados.get(0).motivos());
    }

    /**
     * Verifica que el mismo teléfono se detecta sin importar espacios, guiones ni "+".
     */
    @Test
    void buscarPosiblesDuplicados_mismoTelefonoConOtroFormato() {
        Socio existente = socio(1L, "Ana", "Gómez", "+54 2345-688807", null);
        when(socioRepository.findAll()).thenReturn(List.of(existente));

        List<PosibleDuplicado> duplicados =
                socioService.buscarPosiblesDuplicados(socio(null, "Otra", "Persona", "542345688807", null));

        assertEquals(1, duplicados.size());
        assertEquals(List.of("mismo teléfono"), duplicados.get(0).motivos());
    }

    /**
     * Verifica que si coinciden nombre y teléfono se informan los dos motivos.
     */
    @Test
    void buscarPosiblesDuplicados_coincidenNombreYTelefono() {
        when(socioRepository.findAll()).thenReturn(List.of(socio(1L, "Ana", "Gómez", "1111", null)));

        List<PosibleDuplicado> duplicados =
                socioService.buscarPosiblesDuplicados(socio(null, "Ana", "Gómez", "1111", null));

        assertEquals("mismo nombre y apellido y mismo teléfono", duplicados.get(0).motivosTexto());
    }

    /**
     * Verifica que al editar un socio no se lo compara consigo mismo, y que un teléfono vacío no coincide.
     */
    @Test
    void buscarPosiblesDuplicados_ignoraAlMismoSocioYTelefonosVacios() {
        when(socioRepository.findAll()).thenReturn(List.of(
                socio(1L, "Ana", "Gómez", "1111", null),
                socio(2L, "Juan", "Pérez", null, null)));

        assertTrue(socioService.buscarPosiblesDuplicados(socio(1L, "Ana", "Gómez", "1111", null)).isEmpty());
        assertTrue(socioService.buscarPosiblesDuplicados(socio(null, "Luis", "Díaz", " ", null)).isEmpty());
    }

    /**
     * Verifica que nombres distintos con apellido parecido no se marcan como duplicados.
     */
    @Test
    void buscarPosiblesDuplicados_nombresDistintos_noCoinciden() {
        when(socioRepository.findAll()).thenReturn(List.of(socio(5L, "Leonardo", "Peres", "2345568954", null)));

        assertTrue(socioService.buscarPosiblesDuplicados(socio(null, "Leonel", "Perez", "2345871545", null)).isEmpty());
    }

    /**
     * Verifica que al editar se conservan la fecha de alta y el estado del socio
     * (el formulario no los envía) y que los datos se guardan sin espacios de más.
     */
    @Test
    void guardarConValidacion_edicion_conservaFechaDeAltaYEstado() {
        Socio existente = socio(1L, "Ana", "Gómez", "1111", "ana@mail.com");
        existente.setFechaAlta(LocalDate.of(2026, 1, 10));
        existente.setActivo(false);
        when(socioRepository.findAll()).thenReturn(List.of(existente));
        when(socioRepository.findById(1L)).thenReturn(Optional.of(existente));

        Socio delFormulario = socio(1L, " Ana María ", "Gómez", "2222", " ANA@mail.com ");
        delFormulario.setObjetivo("");

        assertNull(socioService.guardarConValidacion(delFormulario));

        verify(socioRepository).save(existente);
        assertEquals("Ana María", existente.getNombre());
        assertEquals("2222", existente.getTelefono());
        assertEquals("ana@mail.com", existente.getEmail());
        assertNull(existente.getObjetivo());
        assertEquals(LocalDate.of(2026, 1, 10), existente.getFechaAlta());
        assertFalse(existente.isActivo());
    }

    /**
     * Verifica que en el alta un email vacío se guarda como null para no chocar con la restricción única.
     */
    @Test
    void guardarConValidacion_altaSinEmail_loGuardaComoNull() {
        when(socioRepository.findAll()).thenReturn(List.of());

        socioService.guardarConValidacion(socio(null, "Juan", "Pérez", "", ""));

        ArgumentCaptor<Socio> captor = ArgumentCaptor.forClass(Socio.class);
        verify(socioRepository).save(captor.capture());
        assertNull(captor.getValue().getEmail());
        assertNull(captor.getValue().getTelefono());
    }
}
