package Gimnasio.controller;

import Gimnasio.dto.SocioListadoDTO;
import Gimnasio.model.Objetivo;
import Gimnasio.model.Socio;
import Gimnasio.service.CuotaService;
import Gimnasio.service.SocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de socios (CRUD y listado).
 * Maneja las rutas bajo /socios y usa Thymeleaf para renderizar vistas.
 */
@Controller
@RequestMapping("/socios")
public class SocioController {

    @Autowired
    private SocioService socioService;

    @Autowired
    private CuotaService cuotaService;

    /**
     * Listado principal de socios con filtros y estado de cuota calculado.
     * Parámetros opcionales: texto (búsqueda), estadoSocio (activo/inactivo), estadoCuota (al_dia/vencida)
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String texto,
                         @RequestParam(required = false) String estadoSocio,
                         @RequestParam(required = false) String estadoCuota,
                         Model model) {

        // Obtiene socios base según búsqueda por texto
        List<Socio> socios = (texto != null && !texto.isBlank())
                ? socioService.buscarPorTexto(texto)
                : socioService.listarTodos();

        // Filtra por estado del socio (activo/inactivo)
        if ("activo".equals(estadoSocio)) {
            socios = socios.stream().filter(Socio::isActivo).collect(Collectors.toList());
        } else if ("inactivo".equals(estadoSocio)) {
            socios = socios.stream().filter(s -> !s.isActivo()).collect(Collectors.toList());
        }

        // Convierte a DTOs enriquecidos con estado de cuota
        List<SocioListadoDTO> resultado = new ArrayList<>();
        for (Socio s : socios) {
            String estado = cuotaService.calcularEstado(s.getIdSocio());

            // Filtra por estado de cuota si se especificó
            if ("al_dia".equals(estadoCuota) && !estado.equals("AL DÍA")) continue;
            if ("vencida".equals(estadoCuota) && !estado.equals("VENCIDO")) continue;

            Long diasVencido = null;
            boolean critico = false;

            // Calcula días de vencimiento y si es crítico (>15 días)
            if (estado.equals("VENCIDO")) {
                LocalDate fechaPago = cuotaService.ultimoPago(s.getIdSocio())
                        .orElseThrow(() -> new RuntimeException("Inconsistencia: estado VENCIDO sin pago registrado"))
                        .getFechaPago();

                LocalDate vencimiento = cuotaService.calcularVencimiento(fechaPago);
                diasVencido = ChronoUnit.DAYS.between(vencimiento, LocalDate.now());
                critico = diasVencido > 15;
            }

            resultado.add(new SocioListadoDTO(s, estado, diasVencido, critico));
        }
        
        // Estadísticas para cards resumen
        long totalActivos = resultado.stream().filter(SocioListadoDTO::isActivo).count();
        long totalInactivos = resultado.stream().filter(s -> !s.isActivo()).count();
        long totalVencidos = resultado.stream().filter(s -> s.getEstadoCuota().equals("VENCIDO")).count();

        model.addAttribute("totalActivos", totalActivos);
        model.addAttribute("totalInactivos", totalInactivos);
        model.addAttribute("totalVencidos", totalVencidos);

        boolean hayFiltrosAplicados = (texto != null && !texto.isBlank())
                || (estadoSocio != null && !estadoSocio.isBlank())
                || (estadoCuota != null && !estadoCuota.isBlank());

        model.addAttribute("hayFiltrosAplicados", hayFiltrosAplicados);
        model.addAttribute("socios", resultado);
        model.addAttribute("texto", texto);
        model.addAttribute("estadoSocio", estadoSocio);
        model.addAttribute("estadoCuota", estadoCuota);

        return "socios-lista"; // Template: socios-Lista.html
    }

    // Muestra formulario para nuevo socio
    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute("socio", new Socio());
        model.addAttribute("objetivos", Objetivo.opciones());
        return "socio-form";
    }

    // Muestra formulario para editar socio existente
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Socio socio = socioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        model.addAttribute("socio", socio);
        model.addAttribute("objetivos", Objetivo.opciones());
        return "socio-form";
    }

    // Procesa guardado (alta o edición) con validación de email único
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Socio socio, Model model) {
        String error = socioService.guardarConValidacion(socio);
        if (error != null) {
            model.addAttribute("error", error);
            return "socio-form";
        }
        return "redirect:/socios";
    }

    // Da de baja lógica a un socio (activo = false)
    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable Long id) {
        socioService.darDeBaja(id);
        return "redirect:/socios";
    }

    // Reactiva un socio dado de baja
    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Long id) {
        socioService.reactivar(id);
        return "redirect:/socios";
    }

    // Guarda el objetivo elegido en el perfil del socio y vuelve a la ficha
    @PostMapping("/{id}/objetivo")
    public String guardarObjetivo(@PathVariable Long id,
                                  @RequestParam(required = false) String objetivo,
                                  RedirectAttributes redirectAttributes) {
        try {
            socioService.actualizarObjetivo(id, objetivo);
            redirectAttributes.addFlashAttribute("exitoObjetivo", "Objetivo actualizado correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorObjetivo", e.getMessage());
        }
        return "redirect:/socios/" + id + "/pagos";
    }
}