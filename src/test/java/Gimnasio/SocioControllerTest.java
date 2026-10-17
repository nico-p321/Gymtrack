package Gimnasio;

import Gimnasio.controller.SocioController;
import Gimnasio.model.Cuota;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para SocioController.
 * Usa ExtendedModelMap para inspeccionar los atributos que el controlador agrega al modelo.
 */
class SocioControllerTest {

    // Mocks de los servicios inyectados en el controlador
    @Mock
    private SocioService socioService;

    @Mock
    private CuotaService cuotaService;

    // Inyecta los mocks en el controlador bajo prueba
    @InjectMocks
    private SocioController socioController;

    // Model real para capturar atributos
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ExtendedModelMap();
    }

    /**
     * Método helper para crear objetos Socio de prueba con valores consistentes.
     * Reduce duplicación en los tests.
     */
    private Socio crearSocio(Long id, boolean activo) {
        Socio s = new Socio();
        s.setIdSocio(id);
        s.setNombre("Test");
        s.setApellido("Socio" + id);
        s.setActivo(activo);
        return s;
    }

    /**
     * Verifica que listar() calcula correctamente los contadores:
     * - totalActivos: socios con activo=true
     * - totalInactivos: socios con activo=false
     * - totalVencidos: socios cuyo estado de cuota es "VENCIDO"
     * Configura mocks para devolver lista de socios y estados de cuota específicos.
     */
    @Test
    void listar_calculaCorrectamenteLosContadores() {
        Socio activo1 = crearSocio(1L, true);
        Socio activo2 = crearSocio(2L, true);
        Socio inactivo1 = crearSocio(3L, false);

        when(socioService.listarTodos()).thenReturn(List.of(activo1, activo2, inactivo1));
        when(cuotaService.calcularEstado(1L)).thenReturn("AL DÍA");
        when(cuotaService.calcularEstado(2L)).thenReturn("VENCIDO");
        when(cuotaService.calcularEstado(3L)).thenReturn("SIN PAGOS REGISTRADOS");

        // El socio 2 está "VENCIDO", así que el Controller también le va a preguntar
        // al Service por su último pago para calcular los días de atraso.
        // Hay que programar esa respuesta también, o el mock devuelve vacío.
        Cuota cuotaVencida = new Cuota();
        cuotaVencida.setFechaPago(LocalDate.now().minusMonths(2));
        when(cuotaService.ultimoPago(2L)).thenReturn(Optional.of(cuotaVencida));
        when(cuotaService.calcularVencimiento(any(LocalDate.class)))
                .thenAnswer(invocation -> ((LocalDate) invocation.getArgument(0)).plusMonths(1));

        socioController.listar(null, null, null, model);

        assertEquals(2L, model.getAttribute("totalActivos"));
        assertEquals(1L, model.getAttribute("totalInactivos"));
        assertEquals(1L, model.getAttribute("totalVencidos"));
    }

    /**
     * Verifica que cuando hay una búsqueda por texto que no devuelve resultados,
     * el modelo marca hayFiltrosAplicados=true y la lista de socios está vacía.
     */
    @Test
    void listar_sinResultados_marcaHayFiltrosAplicadosSiHayTexto() {
        // Arrange: búsqueda por "zzzz" devuelve lista vacía
        when(socioService.buscarPorTexto("zzzz")).thenReturn(List.of());

        // Act
        socioController.listar("zzzz", null, null, model);

        // Assert
        assertTrue((Boolean) model.getAttribute("hayFiltrosAplicados")); // bandera true
        assertTrue(((List<?>) model.getAttribute("socios")).isEmpty());  // lista vacía
    }

    /**
     * Verifica que cuando no hay socios Y no hay filtros aplicados,
     * hayFiltrosAplicados es false.
     */
    @Test
    void listar_sinSociosYSinFiltros_noMarcaHayFiltrosAplicados() {
        // Arrange: listarTodos devuelve lista vacía (no hay socios en BD)
        when(socioService.listarTodos()).thenReturn(List.of());

        // Act: sin parámetros de filtro
        socioController.listar(null, null, null, model);

        // Assert: no hay filtros aplicados
        assertFalse((Boolean) model.getAttribute("hayFiltrosAplicados"));
    }

    /**
     * Verifica que al elegir un objetivo de la lista, se guarda y se vuelve a la ficha del socio.
     */
    @Test
    void guardarObjetivo_seleccionaUno_guardaYCuelveAlPerfil() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String vista = socioController.guardarObjetivo(1L, "GANAR_MASA_MUSCULAR", redirectAttributes);

        verify(socioService).actualizarObjetivo(1L, "GANAR_MASA_MUSCULAR");
        assertEquals("redirect:/socios/1/pagos", vista);
        assertEquals("Objetivo actualizado correctamente",
                redirectAttributes.getFlashAttributes().get("exitoObjetivo"));
    }

    /**
     * Verifica que si el objetivo elegido no está en el catálogo,
     * se vuelve al perfil mostrando el error y sin guardar nada.
     */
    @Test
    void guardarObjetivo_codigoInvalido_muestraErrorYNoGuarda() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        doThrow(new IllegalArgumentException("El objetivo seleccionado no es válido"))
                .when(socioService).actualizarObjetivo(1L, "PERDER_MASA_MUSI");

        String vista = socioController.guardarObjetivo(1L, "PERDER_MASA_MUSI", redirectAttributes);

        assertEquals("redirect:/socios/1/pagos", vista);
        assertEquals("El objetivo seleccionado no es válido",
                redirectAttributes.getFlashAttributes().get("errorObjetivo"));
        assertNull(redirectAttributes.getFlashAttributes().get("exitoObjetivo"));
    }

    /**
     * Verifica que elegir "Sin definir" (valor vacío del select) deja el objetivo sin definir.
     */
    @Test
    void guardarObjetivo_opcionSinDefinir_pasaElValorVacio() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String vista = socioController.guardarObjetivo(3L, "", redirectAttributes);

        verify(socioService).actualizarObjetivo(3L, "");
        assertEquals("redirect:/socios/3/pagos", vista);
    }

    /**
     * Verifica que el formulario de alta y el de edición mandan el catálogo de objetivos,
     * para que el <select> tenga con qué moverse.
     */
    @Test
    void mostrarFormularios_incluyenElCatalogoDeObjetivos() {
        when(socioService.buscarPorId(1L)).thenReturn(Optional.of(crearSocio(1L, true)));

        socioController.mostrarFormularioAlta(model);
        assertEquals(4, ((List<?>) model.getAttribute("objetivos")).size());

        socioController.mostrarFormularioEdicion(1L, model);
        assertEquals(4, ((List<?>) model.getAttribute("objetivos")).size());
    }
}