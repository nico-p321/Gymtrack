package Gimnasio.controller;

import Gimnasio.model.DiaRutina;
import Gimnasio.model.Socio;
import Gimnasio.service.RutinaService;
import Gimnasio.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Carga de la rutina semanal del socio (grupos musculares y ejercicios por día).
 * Rutas anidadas bajo /socios/{idSocio}/rutina/{dia}; siempre vuelve a la pantalla
 * de entrenamientos con el mismo día seleccionado.
 */
@Controller
@RequestMapping("/socios/{idSocio}/rutina/{dia}")
public class RutinaController {

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private SocioService socioService;

    @PostMapping("/grupos")
    public String guardarGrupos(@PathVariable Long idSocio,
                                @PathVariable String dia,
                                @RequestParam(required = false) List<String> grupos,
                                RedirectAttributes redirectAttributes) {
        DiaRutina diaRutina = buscarDia(dia);
        rutinaService.guardarGrupos(buscarSocio(idSocio), diaRutina, grupos);
        redirectAttributes.addFlashAttribute("exitoRutina", "Grupos del " + diaRutina.getEtiqueta().toLowerCase() + " guardados");
        return volver(idSocio, diaRutina);
    }

    @PostMapping("/ejercicios")
    public String agregarEjercicio(@PathVariable Long idSocio,
                                   @PathVariable String dia,
                                   @RequestParam(required = false) String ejercicio,
                                   @RequestParam(required = false) Integer series,
                                   @RequestParam(required = false) Integer repeticiones,
                                   @RequestParam(required = false) BigDecimal peso,
                                   RedirectAttributes redirectAttributes) {
        DiaRutina diaRutina = buscarDia(dia);
        try {
            rutinaService.agregarEjercicio(buscarSocio(idSocio), diaRutina, ejercicio, series, repeticiones, peso);
            redirectAttributes.addFlashAttribute("exitoRutina", "Ejercicio agregado a la rutina");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorRutina", e.getMessage());
        }
        return volver(idSocio, diaRutina);
    }

    @PostMapping("/ejercicios/{idEjercicio}/eliminar")
    public String eliminarEjercicio(@PathVariable Long idSocio,
                                    @PathVariable String dia,
                                    @PathVariable Long idEjercicio,
                                    RedirectAttributes redirectAttributes) {
        DiaRutina diaRutina = buscarDia(dia);
        try {
            rutinaService.eliminarEjercicio(idSocio, idEjercicio);
            redirectAttributes.addFlashAttribute("exitoRutina", "Ejercicio quitado de la rutina");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorRutina", e.getMessage());
        }
        return volver(idSocio, diaRutina);
    }

    // Pasa los ejercicios del día al historial de entrenamientos con la fecha elegida
    @PostMapping("/registrar")
    public String registrarComoRealizada(@PathVariable Long idSocio,
                                         @PathVariable String dia,
                                         @RequestParam(required = false) LocalDate fecha,
                                         RedirectAttributes redirectAttributes) {
        DiaRutina diaRutina = buscarDia(dia);
        try {
            int cantidad = rutinaService.registrarComoRealizada(buscarSocio(idSocio), diaRutina, fecha);
            redirectAttributes.addFlashAttribute("exitoEntreno",
                    "Rutina del " + diaRutina.getEtiqueta().toLowerCase() + " registrada (" + cantidad + " ejercicios)");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorRutina", e.getMessage());
        }
        return volver(idSocio, diaRutina);
    }

    private Socio buscarSocio(Long idSocio) {
        return socioService.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
    }

    private DiaRutina buscarDia(String dia) {
        DiaRutina diaRutina = DiaRutina.buscar(dia);
        if (diaRutina == null) {
            throw new RuntimeException("Día de rutina inválido");
        }
        return diaRutina;
    }

    private String volver(Long idSocio, DiaRutina dia) {
        return "redirect:/socios/" + idSocio + "/entrenos?dia=" + dia.name();
    }
}
