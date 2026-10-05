
package Gimnasio.controller;

import Gimnasio.service.CuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Controlador para la gestión del valor de la cuota mensual.
 * Permite ver el valor actual y actualizarlo (crea un nuevo registro histórico).
 */
@Controller
@RequestMapping("/cuota-valor")
public class CuotaValorController {

    @Autowired
    private CuotaService cuotaService;

    // Muestra el valor actual de la cuota y formulario para actualizarlo
    @GetMapping
    public String ver(Model model) {
        cuotaService.obtenerValorVigente().ifPresent(valor -> {
            model.addAttribute("montoActual", valor.getMonto());
            model.addAttribute("vigenteDesde", valor.getVigenteDesde());
        });
        return "cuota-valor";
    }

    // Guarda un nuevo valor de cuota (crea registro histórico con fecha actual)
    @PostMapping("/guardar")
    public String guardar(@RequestParam BigDecimal monto) {
        cuotaService.actualizarValorCuota(monto);
        return "redirect:/cuota-valor";
    }
}