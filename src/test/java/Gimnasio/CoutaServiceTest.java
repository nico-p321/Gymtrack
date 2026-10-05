package Gimnasio;

import Gimnasio.model.CuotaValor;
import Gimnasio.repository.CuotaRepository;
import Gimnasio.repository.CuotaValorRepository;
import Gimnasio.service.CuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CuotaService.
 * Usa Mockito para mockear las dependencias (repositorios) y probar la lógica del servicio aisladamente.
 */
class CuotaServiceTest {

    // @Mock crea un mock (doble de prueba) del repositorio para simular su comportamiento
    @Mock
    private CuotaValorRepository cuotaValorRepository;

    @Mock
    private CuotaRepository cuotaRepository;

    // @InjectMocks inyecta los mocks arriba en la instancia del servicio bajo prueba
    @InjectMocks
    private CuotaService cuotaService;

    // @BeforeEach se ejecuta antes de cada test para inicializar los mocks
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verifica que obtenerValorVigente() devuelve la cuota con fecha vigenteDesde más reciente.
     * Configura el mock para devolver un valor específico cuando se llama al método del repositorio.
     */
    @Test
    void obtenerValorVigente_devuelveElValorMasReciente() {
        // Arrange: prepara los datos de prueba
        CuotaValor valor = new CuotaValor();
        valor.setMonto(new BigDecimal("15000.00"));
        valor.setVigenteDesde(LocalDate.of(2026, 9, 1));

        // Configura el comportamiento del mock: cuando se llame a findTopByOrderByVigenteDesdeDesc(),
        // devuelve el valor creado arriba envuelto en Optional
        when(cuotaValorRepository.findTopByOrderByVigenteDesdeDesc())
                .thenReturn(Optional.of(valor));

        // Act: ejecuta el método bajo prueba
        Optional<CuotaValor> resultado = cuotaService.obtenerValorVigente();

        // Assert: verifica los resultados
        assertTrue(resultado.isPresent()); // Verifica que el Optional no está vacío
        assertEquals(new BigDecimal("15000.00"), resultado.get().getMonto()); // Verifica el monto
    }

    /**
     * Verifica que actualizarValorCuota() crea un nuevo registro CuotaValor con el monto dado
     * y la fecha de hoy (LocalDate.now()).
     * Usa ArgumentCaptor para capturar el objeto pasado al repositorio y verificar sus campos.
     */
    @Test
    void actualizarValorCuota_creaUnRegistroNuevoConFechaDeHoy() {
        // Arrange
        BigDecimal nuevoMonto = new BigDecimal("18000.00");

        // Configura el mock save para devolver el mismo objeto que recibe (simula guardado en BD)
        when(cuotaValorRepository.save(any(CuotaValor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cuotaService.actualizarValorCuota(nuevoMonto);

        // Assert: captura el argumento pasado al mock para inspeccionarlo
        ArgumentCaptor<CuotaValor> captor = ArgumentCaptor.forClass(CuotaValor.class);
        verify(cuotaValorRepository).save(captor.capture()); // Verifica que save fue llamado y captura el arg

        CuotaValor guardado = captor.getValue(); // Obtiene el objeto capturado
        assertEquals(nuevoMonto, guardado.getMonto()); // Verifica que el monto coincide
        assertEquals(LocalDate.now(), guardado.getVigenteDesde()); // Verifica que la fecha es hoy
    }
}