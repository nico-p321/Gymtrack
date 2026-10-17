package Gimnasio.controller;

import Gimnasio.model.Socio;
import Gimnasio.service.MedidaFisicaService;
import Gimnasio.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/socios/{idSocio}/medidas")
public class MedidaFisicaController {

    @Autowired
    private MedidaFisicaService medidaFisicaService;

    @Autowired
    private SocioService socioService;

    @PostMapping("/guardar")
    public String guardar(@PathVariable Long idSocio,
                          @RequestParam(required = false) LocalDate fecha,
                          @RequestParam(required = false) BigDecimal peso,
                          @RequestParam(required = false) BigDecimal altura,
                          @RequestParam(required = false) BigDecimal grasaCorporal,
                          RedirectAttributes redirectAttributes) {

        Socio socio = socioService.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        try {
            medidaFisicaService.registrarMedida(socio, fecha, peso, altura, grasaCorporal);
            redirectAttributes.addFlashAttribute("exitoMedida", "Medida registrada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMedida", e.getMessage());
        }

        return "redirect:/socios/" + idSocio + "/pagos";
    }
}