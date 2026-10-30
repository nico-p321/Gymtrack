package Gimnasio;

import Gimnasio.dto.DiaRutinaVista;
import Gimnasio.model.DiaRutina;
import Gimnasio.model.EjercicioRutina;
import Gimnasio.model.GrupoMuscular;
import Gimnasio.model.RutinaDia;
import Gimnasio.model.Socio;
import Gimnasio.repository.EjercicioRutinaRepository;
import Gimnasio.repository.RutinaDiaRepository;
import Gimnasio.service.EntrenoService;
import Gimnasio.service.RutinaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RutinaService.
 * Usa Mockito para mockear los repositorios y probar la lógica de la rutina semanal aisladamente.
 */
class RutinaServiceTest {

    @Mock
    private RutinaDiaRepository rutinaDiaRepository;

    @Mock
    private EjercicioRutinaRepository ejercicioRutinaRepository;

    @Mock
    private EntrenoService entrenoService;

    @InjectMocks
    private RutinaService rutinaService;

    private Socio socio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        socio = new Socio();
        socio.setIdSocio(1L);
    }

    private EjercicioRutina crearEjercicio(DiaRutina dia, String nombre) {
        EjercicioRutina e = new EjercicioRutina();
        e.setSocio(socio);
        e.setDia(dia);
        e.setEjercicio(nombre);
        e.setSeries(4);
        e.setRepeticiones(10);
        e.setPeso(new BigDecimal("40"));
        return e;
    }

    /**
     * Verifica que la semana trae los seis días en orden, con sus grupos y ejercicios.
     */
    @Test
    void obtenerSemana_armaLosSeisDiasConGruposYEjercicios() {
        RutinaDia lunes = new RutinaDia();
        lunes.setDia(DiaRutina.LUNES);
        lunes.setGrupos("PECHO,HOMBRO,TRICEPS");
        EjercicioRutina press = crearEjercicio(DiaRutina.LUNES, "Press de banca");

        when(rutinaDiaRepository.findBySocioIdSocio(1L)).thenReturn(List.of(lunes));
        when(ejercicioRutinaRepository.findBySocioIdSocioOrderByIdEjercicioRutinaAsc(1L)).thenReturn(List.of(press));

        List<DiaRutinaVista> semana = rutinaService.obtenerSemana(1L);

        assertEquals(6, semana.size());
        assertEquals(DiaRutina.LUNES, semana.get(0).dia());
        assertEquals(List.of(GrupoMuscular.PECHO, GrupoMuscular.HOMBRO, GrupoMuscular.TRICEPS), semana.get(0).grupos());
        assertEquals("Pecho · Hombro · Tríceps", semana.get(0).resumenGrupos());
        assertEquals(List.of(press), semana.get(0).ejercicios());
        assertTrue(semana.get(1).esDescanso());
    }

    /**
     * Verifica que guardar grupos crea el día si no existe e ignora códigos inválidos o repetidos.
     */
    @Test
    void guardarGrupos_creaElDiaYDescartaCodigosInvalidos() {
        when(rutinaDiaRepository.findBySocioIdSocioAndDia(1L, DiaRutina.MARTES)).thenReturn(Optional.empty());

        rutinaService.guardarGrupos(socio, DiaRutina.MARTES, List.of("ESPALDA", "BICEPS", "INVENTADO", "ESPALDA"));

        ArgumentCaptor<RutinaDia> captor = ArgumentCaptor.forClass(RutinaDia.class);
        verify(rutinaDiaRepository).save(captor.capture());
        assertEquals(DiaRutina.MARTES, captor.getValue().getDia());
        assertEquals(socio, captor.getValue().getSocio());
        assertEquals("ESPALDA,BICEPS", captor.getValue().getGrupos());
    }

    /**
     * Verifica que guardar sin grupos marcados deja el día existente vacío.
     */
    @Test
    void guardarGrupos_sinGrupos_vaciaElDiaExistente() {
        RutinaDia existente = new RutinaDia();
        existente.setGrupos("PECHO");
        when(rutinaDiaRepository.findBySocioIdSocioAndDia(1L, DiaRutina.LUNES)).thenReturn(Optional.of(existente));

        rutinaService.guardarGrupos(socio, DiaRutina.LUNES, null);

        assertEquals("", existente.getGrupos());
        verify(rutinaDiaRepository).save(existente);
    }

    /**
     * Verifica que se agrega el ejercicio al día indicado.
     */
    @Test
    void agregarEjercicio_guardaElEjercicioEnElDia() {
        when(ejercicioRutinaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EjercicioRutina guardado = rutinaService.agregarEjercicio(
                socio, DiaRutina.LUNES, "  Press de banca ", 4, 10, new BigDecimal("40"));

        assertEquals(DiaRutina.LUNES, guardado.getDia());
        assertEquals("Press de banca", guardado.getEjercicio());
        assertEquals(socio, guardado.getSocio());
    }

    /**
     * Verifica que se aplican las mismas validaciones que en el registro de entrenamientos.
     */
    @Test
    void agregarEjercicio_conSeriesEnCero_lanzaError() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> rutinaService.agregarEjercicio(socio, DiaRutina.LUNES, "Press", 0, 10, null));

        assertEquals("Las series deben ser mayores a cero", error.getMessage());
        verify(ejercicioRutinaRepository, never()).save(any());
    }

    /**
     * Verifica que no se puede quitar un ejercicio de la rutina de otro socio.
     */
    @Test
    void eliminarEjercicio_deOtroSocio_lanzaError() {
        Socio otro = new Socio();
        otro.setIdSocio(2L);
        EjercicioRutina ajeno = crearEjercicio(DiaRutina.LUNES, "Press");
        ajeno.setSocio(otro);
        when(ejercicioRutinaRepository.findById(9L)).thenReturn(Optional.of(ajeno));

        assertThrows(RuntimeException.class, () -> rutinaService.eliminarEjercicio(1L, 9L));
        verify(ejercicioRutinaRepository, never()).delete(any());
    }

    /**
     * Verifica que registrar la rutina como realizada copia cada ejercicio al historial con la fecha.
     */
    @Test
    void registrarComoRealizada_copiaLosEjerciciosAlHistorial() {
        LocalDate fecha = LocalDate.of(2026, 10, 26);
        when(ejercicioRutinaRepository.findBySocioIdSocioAndDiaOrderByIdEjercicioRutinaAsc(1L, DiaRutina.LUNES))
                .thenReturn(List.of(crearEjercicio(DiaRutina.LUNES, "Press de banca"),
                        crearEjercicio(DiaRutina.LUNES, "Press militar")));

        int cantidad = rutinaService.registrarComoRealizada(socio, DiaRutina.LUNES, fecha);

        assertEquals(2, cantidad);
        verify(entrenoService).registrar(socio, fecha, "Press de banca", 4, 10, new BigDecimal("40"));
        verify(entrenoService).registrar(socio, fecha, "Press militar", 4, 10, new BigDecimal("40"));
    }

    /**
     * Verifica que no se puede registrar un día sin ejercicios.
     */
    @Test
    void registrarComoRealizada_sinEjercicios_lanzaError() {
        when(ejercicioRutinaRepository.findBySocioIdSocioAndDiaOrderByIdEjercicioRutinaAsc(1L, DiaRutina.MARTES))
                .thenReturn(List.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> rutinaService.registrarComoRealizada(socio, DiaRutina.MARTES, LocalDate.now()));

        assertEquals("El martes no tiene ejercicios cargados", error.getMessage());
        verifyNoInteractions(entrenoService);
    }
}
