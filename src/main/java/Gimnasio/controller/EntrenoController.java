package Gimnasio.controller;

import Gimnasio.dto.DiaEntrenado;
import Gimnasio.dto.DiaRutinaVista;
import Gimnasio.model.DiaRutina;
import Gimnasio.model.Entreno;
import Gimnasio.model.GrupoMuscular;
import Gimnasio.model.Socio;
import Gimnasio.service.EntrenoService;
import Gimnasio.service.RutinaService;
import Gimnasio.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para el historial de entrenamientos de un socio específico.
 * Rutas anidadas bajo /socios/{idSocio}/entrenos
 */
@Controller
@RequestMapping("/socios/{idSocio}/entrenos")
public class EntrenoController {

    @Autowired
    private EntrenoService entrenoService;

    @Autowired
    private SocioService socioService;

    @Autowired
    private RutinaService rutinaService;

    // Muestra la rutina semanal (día elegido o el de hoy), el formulario de carga
    // y el historial de entrenamientos agrupado por fecha
    @GetMapping
    public String verEntrenamientos(@PathVariable Long idSocio,
                                    @RequestParam(required = false) String dia,
                                    Model model) {
        Socio socio = socioService.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        LocalDate hoy = LocalDate.now();
        DiaRutina diaElegido = DiaRutina.buscar(dia) != null ? DiaRutina.buscar(dia) : DiaRutina.deFecha(hoy);
        List<DiaRutinaVista> semana = rutinaService.obtenerSemana(idSocio);
        List<Entreno> entrenos = entrenoService.listarPorSocio(idSocio);

        model.addAttribute("socio", socio);
        model.addAttribute("entrenos", entrenos);
        model.addAttribute("historial", DiaEntrenado.agrupar(entrenos));
        model.addAttribute("semana", semana);
        model.addAttribute("diaElegido", semana.stream().filter(d -> d.dia() == diaElegido).findFirst().orElse(null));
        model.addAttribute("gruposMusculares", GrupoMuscular.opciones());
        model.addAttribute("hoy", hoy);

        return "socio-entrenos";
    }

    // Recibe el formulario y registra un ejercicio asociado al socio
    @PostMapping("/guardar")
    public String guardar(@PathVariable Long idSocio,
                          @RequestParam(required = false) LocalDate fecha,
                          @RequestParam(required = false) String ejercicio,
                          @RequestParam(required = false) Integer series,
                          @RequestParam(required = false) Integer repeticiones,
                          @RequestParam(required = false) BigDecimal peso,
                          RedirectAttributes redirectAttributes) {

        Socio socio = socioService.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        try {
            entrenoService.registrar(socio, fecha, ejercicio, series, repeticiones, peso);
            redirectAttributes.addFlashAttribute("exitoEntreno", "Entrenamiento registrado correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorEntreno", e.getMessage());
        }

        return "redirect:/socios/" + idSocio + "/entrenos";
    }

    // Elimina un registro del historial del socio
    @PostMapping("/{idEntreno}/eliminar")
    public String eliminar(@PathVariable Long idSocio,
                           @PathVariable Long idEntreno,
                           RedirectAttributes redirectAttributes) {

        try {
            entrenoService.eliminar(idSocio, idEntreno);
            redirectAttributes.addFlashAttribute("exitoEntreno", "Entrenamiento eliminado");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorEntreno", e.getMessage());
        }

        return "redirect:/socios/" + idSocio + "/entrenos";
    }
}