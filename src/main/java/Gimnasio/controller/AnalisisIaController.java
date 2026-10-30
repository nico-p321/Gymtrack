package Gimnasio.controller;

import Gimnasio.model.Socio;
import Gimnasio.service.AnalisisIaService;
import Gimnasio.service.IaException;
import Gimnasio.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Análisis con IA desde el perfil del socio.
 * Si la IA falla, se muestra el mensaje en el perfil y la aplicación sigue funcionando normalmente.
 */
@Controller
@RequestMapping("/socios/{idSocio}/analisis")
public class AnalisisIaController {

    @Autowired
    private AnalisisIaService analisisIaService;

    @Autowired
    private SocioService socioService;

    @PostMapping("/progreso")
    public String analizarProgreso(@PathVariable Long idSocio, RedirectAttributes redirectAttributes) {
        Socio socio = socioService.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        try {
            redirectAttributes.addFlashAttribute("analisisIa", analisisIaService.analizarProgreso(socio));
        } catch (IaException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorIa", e.getMessage());
        }

        return "redirect:/socios/" + idSocio + "/pagos";
    }
}
