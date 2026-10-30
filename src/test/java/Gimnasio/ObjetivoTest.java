package Gimnasio;

import Gimnasio.model.Objetivo;
import Gimnasio.model.Socio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del catálogo de objetivos y de su representación en la entidad Socio.
 */
class ObjetivoTest {

    /**
     * Verifica que el catálogo expone los cuatro objetivos que Carlos puede asignar.
     */
    @Test
    void opciones_contieneLosCuatroObjetivosDelCatalogo() {
        assertEquals(4, Objetivo.opciones().size());
        assertTrue(Objetivo.esValido("GANAR_MASA_MUSCULAR"));
        assertTrue(Objetivo.esValido("BAJAR_DE_PESO"));
        assertTrue(Objetivo.esValido("MANTENERSE"));
        assertTrue(Objetivo.esValido("MEJORAR_RESISTENCIA"));
    }

    /**
     * Verifica que el objetivo se muestra como "Sin definir" cuando el socio no tiene ninguno,
     * sin fallar ante null o valores desconocidos.
     */
    @Test
    void etiquetaDe_sinObjetivo_devuelveSinDefinir() {
        assertEquals("Sin definir", Objetivo.etiquetaDe(null));
        assertEquals("Sin definir", Objetivo.etiquetaDe("  "));
        assertEquals("Sin definir", Objetivo.etiquetaDe("OBJETIVO_INVENTADO"));
    }

    /**
     * Verifica que un socio recién creado, sin objetivo, expone la etiqueta "Sin definir".
     */
    @Test
    void socioSinObjetivo_muestraSinDefinir() {
        Socio socio = new Socio();
        socio.setNombre("Test");
        socio.setApellido("Socio");

        assertNull(socio.getObjetivo());
        assertEquals("Sin definir", socio.getObjetivoEtiqueta());
    }

    /**
     * Verifica que un socio con objetivo expone la etiqueta de ese objetivo.
     */
    @Test
    void socioConObjetivo_muestraLaEtiquetaDelObjetivo() {
        Socio socio = new Socio();
        socio.setObjetivo("BAJAR_DE_PESO");

        assertEquals("Bajar de peso", socio.getObjetivoEtiqueta());
    }
}