package Gimnasio;

import Gimnasio.dto.DiaEntrenado;
import Gimnasio.model.DiaRutina;
import Gimnasio.model.Entreno;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el agrupado del historial por fecha y el día de rutina de cada fecha.
 */
class DiaEntrenadoTest {

    private Entreno entreno(LocalDate fecha, String ejercicio) {
        Entreno e = new Entreno();
        e.setFecha(fecha);
        e.setEjercicio(ejercicio);
        return e;
    }

    /**
     * Verifica que los ejercicios de la misma fecha quedan juntos, respetando el orden del historial.
     */
    @Test
    void agrupar_juntaLosEjerciciosDeLaMismaFecha() {
        LocalDate martes = LocalDate.of(2026, 10, 27);
        LocalDate lunes = LocalDate.of(2026, 10, 26);
        Entreno remo = entreno(martes, "Remo");
        Entreno curl = entreno(martes, "Curl");
        Entreno press = entreno(lunes, "Press");

        List<DiaEntrenado> dias = DiaEntrenado.agrupar(List.of(remo, curl, press));

        assertEquals(2, dias.size());
        assertEquals(martes, dias.get(0).fecha());
        assertEquals(List.of(remo, curl), dias.get(0).ejercicios());
        assertEquals("Martes", dias.get(0).nombreDia());
        assertEquals(List.of(press), dias.get(1).ejercicios());
        assertEquals("Lunes", dias.get(1).nombreDia());
        assertEquals("1 ejercicio", dias.get(1).cantidadEjercicios());
        assertEquals("2 ejercicios", dias.get(0).cantidadEjercicios());
    }

    /**
     * Verifica que dentro de una fecha los ejercicios quedan en el orden en que se cargaron,
     * aunque el historial venga del más reciente al más antiguo.
     */
    @Test
    void agrupar_ordenaLosEjerciciosDelDiaComoSeCargaron() {
        LocalDate lunes = LocalDate.of(2026, 10, 26);
        Entreno primero = entreno(lunes, "Press de banca");
        primero.setIdEntreno(1L);
        Entreno segundo = entreno(lunes, "Fondos");
        segundo.setIdEntreno(2L);

        List<DiaEntrenado> dias = DiaEntrenado.agrupar(List.of(segundo, primero));

        assertEquals(List.of(primero, segundo), dias.get(0).ejercicios());
    }

    /**
     * Verifica la traducción de fecha a día de rutina (el domingo cae en lunes).
     */
    @Test
    void diaRutina_deFecha() {
        assertEquals(DiaRutina.MIERCOLES, DiaRutina.deFecha(LocalDate.of(2026, 10, 28)));
        assertEquals(DiaRutina.SABADO, DiaRutina.deFecha(LocalDate.of(2026, 10, 31)));
        assertEquals(DiaRutina.LUNES, DiaRutina.deFecha(LocalDate.of(2026, 11, 1)));
        assertNull(DiaRutina.buscar("DOMINGO"));
    }
}
