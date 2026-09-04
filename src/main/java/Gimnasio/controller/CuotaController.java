package Gimnasio.controller;

import Gimnasio.model.Cuota;
import Gimnasio.model.Socio;
import Gimnasio.service.CuotaService;
import Gimnasio.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/socios/{idSocio}/pagos")
public class CuotaController {

    @Autowired
    private CuotaService cuotaService;

    @Autowired
    private SocioService socioService;

    // Muestra el perfil del socio con su historial de pagos y el form para uno nuevo
    @GetMapping
    public String verPagos(@PathVariable Long idSocio, Model model) {
        Socio socio = socioService.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        model.addAttribute("socio", socio);
        model.addAttribute("pagos", cuotaService.historialDePagos(idSocio));
        model.addAttribute("estado", cuotaService.calcularEstado(idSocio));

        return "socio-perfil";
    }

    // Recibe el formulario y registra el pago
    @PostMapping("/registrar")
    public String registrarPago(@PathVariable Long idSocio,
                                @RequestParam LocalDate fechaPago,
                                @RequestParam(required = false) BigDecimal monto) {

        Socio socio = socioService.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        cuotaService.registrarPago(socio, fechaPago, monto);

        return "redirect:/socios/" + idSocio + "/pagos";
    }
}