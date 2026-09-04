package Gimnasio.controller;

import Gimnasio.dto.SocioListadoDTO;
import Gimnasio.model.Socio;
import Gimnasio.service.CuotaService;
import Gimnasio.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/socios")
public class SocioController {

    @Autowired
    private SocioService socioService;

    @Autowired
    private CuotaService cuotaService;

    @GetMapping
    public String listar(@RequestParam(required = false) String texto,
                         @RequestParam(required = false) String estadoSocio,
                         @RequestParam(required = false) String estadoCuota,
                         Model model) {

        // 1. Traer socios según texto de búsqueda
        List<Socio> socios = (texto != null && !texto.isBlank())
                ? socioService.buscarPorTexto(texto)
                : socioService.listarTodos();

        // 2. Filtrar por estado del socio (activo/inactivo)
        if ("activo".equals(estadoSocio)) {
            socios = socios.stream().filter(Socio::isActivo).collect(Collectors.toList());
        } else if ("inactivo".equals(estadoSocio)) {
            socios = socios.stream().filter(s -> !s.isActivo()).collect(Collectors.toList());
        }

        // 3. Calcular estado de cuota de cada socio y filtrar si corresponde
        List<SocioListadoDTO> resultado = new ArrayList<>();
        for (Socio s : socios) {
            String estado = cuotaService.calcularEstado(s.getIdSocio());

            if ("al_dia".equals(estadoCuota) && !estado.equals("AL DÍA")) continue;
            if ("vencida".equals(estadoCuota) && !estado.equals("VENCIDO")) continue;

            resultado.add(new SocioListadoDTO(s, estado));
        }

        model.addAttribute("socios", resultado);
        model.addAttribute("texto", texto);
        model.addAttribute("estadoSocio", estadoSocio);
        model.addAttribute("estadoCuota", estadoCuota);

        return "socios-lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute("socio", new Socio());
        return "socio-form";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Socio socio = socioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        model.addAttribute("socio", socio);
        return "socio-form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Socio socio, Model model) {
        String error = socioService.guardarConValidacion(socio);
        if (error != null) {
            model.addAttribute("error", error);
            return "socio-form";
        }
        return "redirect:/socios";
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable Long id) {
        socioService.darDeBaja(id);
        return "redirect:/socios";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Long id) {
        socioService.reactivar(id);
        return "redirect:/socios";
    }
}