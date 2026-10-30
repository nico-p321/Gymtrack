package Gimnasio;

import Gimnasio.model.Entreno;
import Gimnasio.model.Socio;
import Gimnasio.repository.EntrenoRepository;
import Gimnasio.service.EntrenoService;
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
 * Tests unitarios para EntrenoService.
 * Usa Mockito para mockear el repositorio y probar la lógica del servicio aisladamente.
 */
class EntrenoServiceTest {

    @Mock
    private EntrenoRepository entrenoRepository;

    @InjectMocks
    private EntrenoService entrenoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper para crear un socio de prueba
    private Socio crearSocio() {
        Socio socio = new Socio();
        socio.setIdSocio(1L);
        socio.setNombre("Carlos");
        socio.setApellido("Perez");
        return socio;
    }

    /**
     * Verifica que registrar() guarda el ejercicio asociado al socio y con la fecha cargada.
     * Captura el objeto pasado al repositorio para revisar sus campos.
     */
    @Test
    void registrar_asignaElEjercicioAlSocioConLaFechaCargada() {
        // Arrange
        Socio socio = crearSocio();
        LocalDate fecha = LocalDate.of(2026, 10, 1);

        when(entrenoRepository.save(any(Entreno.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        entrenoService.registrar(socio, fecha, "Press de banca", 4, 8, new BigDecimal("80.5"));

        // Assert
        ArgumentCaptor<Entreno> captor = ArgumentCaptor.forClass(Entreno.class);
        verify(entrenoRepository).save(captor.capture());

        Entreno guardado = captor.getValue();
        assertEquals(socio, guardado.getSocio());          // el registro queda asociado al socio
        assertEquals(fecha, guardado.getFecha());          // con la fecha indicada
        assertEquals("Press de banca", guardado.getEjercicio());
        assertEquals(4, guardado.getSeries());
        assertEquals(8, guardado.getRepeticiones());
        assertEquals(new BigDecimal("80.5"), guardado.getPeso());
    }

    /**
     * Verifica que registrar() sin fecha usa la fecha de hoy.
     */
    @Test
    void registrar_sinFecha_usaLaFechaDeHoy() {
        // Arrange
        when(entrenoRepository.save(any(Entreno.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        entrenoService.registrar(crearSocio(), null, "Sentadilla", 3, 10, null);

        // Assert
        ArgumentCaptor<Entreno> captor = ArgumentCaptor.forClass(Entreno.class);
        verify(entrenoRepository).save(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue().getFecha());
    }

    /**
     * Verifica que cargar varios ejercicios el mismo día genera un registro por ejercicio,
     * todos bajo esa misma fecha.
     */
    @Test
    void registrar_variosEjerciciosElMismoDia_guardaUnoPorEjercicio() {
        // Arrange
        Socio socio = crearSocio();
        LocalDate fecha = LocalDate.of(2026, 10, 2);

        when(entrenoRepository.save(any(Entreno.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        entrenoService.registrar(socio, fecha, "Press de banca", 4, 8, new BigDecimal("80"));
        entrenoService.registrar(socio, fecha, "Sentadilla", 3, 10, new BigDecimal("100"));
        entrenoService.registrar(socio, fecha, "Remo", 3, 12, new BigDecimal("40"));

        // Assert
        ArgumentCaptor<Entreno> captor = ArgumentCaptor.forClass(Entreno.class);
        verify(entrenoRepository, times(3)).save(captor.capture());

        List<Entreno> guardados = captor.getAllValues();
        assertEquals(3, guardados.size());
        assertTrue(guardados.stream().allMatch(e -> e.getFecha().equals(fecha)));
        assertTrue(guardados.stream().allMatch(e -> e.getSocio().equals(socio)));
    }

    /**
     * Verifica que las series en cero o negativas se rechazan con mensaje claro.
     */
    @Test
    void registrar_conSeriesNoPositivas_lanzaError() {
        // Act + Assert
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                entrenoService.registrar(crearSocio(), LocalDate.now(), "Press de banca", 0, 8, null));

        assertEquals("Las series deben ser mayores a cero", error.getMessage());
        verify(entrenoRepository, never()).save(any(Entreno.class));
    }

    /**
     * Verifica que las repeticiones en cero o negativas se rechazan con mensaje claro.
     */
    @Test
    void registrar_conRepeticionesNoPositivas_lanzaError() {
        // Act + Assert
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                entrenoService.registrar(crearSocio(), LocalDate.now(), "Press de banca", 4, -3, null));

        assertEquals("Las repeticiones deben ser mayores a cero", error.getMessage());
        verify(entrenoRepository, never()).save(any(Entreno.class));
    }

    /**
     * Verifica que un ejercicio en blanco se rechaza.
     */
    @Test
    void registrar_sinEjercicio_lanzaError() {
        // Act + Assert
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                entrenoService.registrar(crearSocio(), LocalDate.now(), "  ", 4, 8, null));

        assertEquals("Debe indicar el nombre del ejercicio", error.getMessage());
    }

    /**
     * Verifica que un peso negativo se rechaza.
     */
    @Test
    void registrar_conPesoNegativo_lanzaError() {
        // Act + Assert
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                entrenoService.registrar(crearSocio(), LocalDate.now(), "Press de banca", 4, 8, new BigDecimal("-5")));

        assertEquals("El peso no puede ser negativo", error.getMessage());
    }

    /**
     * Verifica que eliminar() borra el registro que pertenece al socio.
     */
    @Test
    void eliminar_borraElEntrenamientoDelSocio() {
        // Arrange
        Socio socio = crearSocio();
        Entreno entreno = new Entreno();
        entreno.setIdEntreno(7L);
        entreno.setSocio(socio);

        when(entrenoRepository.findById(7L)).thenReturn(Optional.of(entreno));

        // Act
        entrenoService.eliminar(1L, 7L);

        // Assert
        verify(entrenoRepository).delete(entreno);
    }

    /**
     * Verifica que no se puede borrar un entrenamiento de otro socio.
     */
    @Test
    void eliminar_entrenamientoDeOtroSocio_lanzaError() {
        // Arrange: el entrenamiento pertenece al socio 2, se intenta borrar desde el socio 1
        Socio otroSocio = crearSocio();
        otroSocio.setIdSocio(2L);

        Entreno entreno = new Entreno();
        entreno.setIdEntreno(7L);
        entreno.setSocio(otroSocio);

        when(entrenoRepository.findById(7L)).thenReturn(Optional.of(entreno));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> entrenoService.eliminar(1L, 7L));
        verify(entrenoRepository, never()).delete(any(Entreno.class));
    }

    /**
     * Verifica que listarPorSocio() devuelve el historial del socio desde el repositorio.
     */
    @Test
    void listarPorSocio_devuelveElHistorialDelSocio() {
        // Arrange
        Entreno entreno = new Entreno();
        entreno.setIdEntreno(7L);
        when(entrenoRepository.findBySocioIdSocioOrderByFechaDescIdEntrenoDesc(1L))
                .thenReturn(List.of(entreno));

        // Act
        List<Entreno> resultado = entrenoService.listarPorSocio(1L);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(7L, resultado.get(0).getIdEntreno());
    }
}