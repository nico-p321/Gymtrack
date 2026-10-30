package Gimnasio.service;

import Gimnasio.dto.PosibleDuplicado;
import Gimnasio.model.Objetivo;
import Gimnasio.model.Socio;
import Gimnasio.repository.SocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Servicio que encapsula la lógica de negocio para la gestión de socios.
 * Actúa como intermediario entre controladores y repositorio.
 */
@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    // Retorna solo socios con activo = true
    public List<Socio> listarActivos() {
        return socioRepository.findByActivoTrue();
    }

    // Retorna todos los socios (activos e inactivos)
    public List<Socio> listarTodos() {
        return socioRepository.findAll();
    }

    // Busca un socio por su ID
    public Optional<Socio> buscarPorId(Long id) {
        return socioRepository.findById(id);
    }

    // Guarda un socio sin validaciones (uso interno)
    public Socio guardar(Socio socio) {
        return socioRepository.save(socio);
    }

    // Valida email duplicado antes de guardar (alta o edición)
    // Retorna null si OK, o mensaje de error si el email ya existe en otro socio
    public String guardarConValidacion(Socio socio) {
        String error = validarEmail(socio);
        if (error != null) {
            return error;
        }

        if (socio.getIdSocio() == null) {
            normalizarDatos(socio, socio);
            socioRepository.save(socio);
        } else {
            // En la edición se copian solo los campos del formulario, así no se pierden
            // la fecha de alta ni el estado activo/inactivo (que el formulario no envía)
            Socio existente = socioRepository.findById(socio.getIdSocio())
                    .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
            normalizarDatos(socio, existente);
            socioRepository.save(existente);
        }
        return null; // null = sin errores
    }

    // El email no se puede repetir; se compara sin mayúsculas ni espacios.
    // Retorna null si OK, o el mensaje de error
    public String validarEmail(Socio socio) {
        String email = normalizarEmail(socio.getEmail());
        if (email == null) {
            return null;
        }
        boolean usado = socioRepository.findAll().stream()
                .anyMatch(otro -> !esElMismo(otro, socio) && email.equals(normalizarEmail(otro.getEmail())));
        return usado ? "Ese email ya pertenece a otro socio" : null;
    }

    // Socios ya cargados con el mismo nombre y apellido o el mismo teléfono.
    // Se ignora el propio socio cuando se está editando.
    public List<PosibleDuplicado> buscarPosiblesDuplicados(Socio socio) {
        String nombreCompleto = normalizarTexto(socio.getNombre()) + "|" + normalizarTexto(socio.getApellido());
        String telefono = normalizarTelefono(socio.getTelefono());

        List<PosibleDuplicado> duplicados = new ArrayList<>();
        for (Socio otro : socioRepository.findAll()) {
            if (esElMismo(otro, socio)) {
                continue;
            }
            List<String> motivos = new ArrayList<>();
            if (nombreCompleto.equals(normalizarTexto(otro.getNombre()) + "|" + normalizarTexto(otro.getApellido()))) {
                motivos.add("mismo nombre y apellido");
            }
            if (!telefono.isEmpty() && telefono.equals(normalizarTelefono(otro.getTelefono()))) {
                motivos.add("mismo teléfono");
            }
            if (!motivos.isEmpty()) {
                duplicados.add(new PosibleDuplicado(otro, motivos));
            }
        }
        return duplicados;
    }

    private static boolean esElMismo(Socio otro, Socio socio) {
        return socio.getIdSocio() != null && socio.getIdSocio().equals(otro.getIdSocio());
    }

    // Copia los datos del formulario limpiando espacios; email y objetivo vacíos quedan en null
    private static void normalizarDatos(Socio desde, Socio hacia) {
        hacia.setNombre(desde.getNombre() == null ? null : desde.getNombre().trim());
        hacia.setApellido(desde.getApellido() == null ? null : desde.getApellido().trim());
        hacia.setTelefono(desde.getTelefono() == null || desde.getTelefono().isBlank() ? null : desde.getTelefono().trim());
        hacia.setEmail(normalizarEmail(desde.getEmail()));
        hacia.setObjetivo(desde.getObjetivo() == null || desde.getObjetivo().isBlank() ? null : desde.getObjetivo().trim());
    }

    // "  Juan@Mail.com " -> "juan@mail.com"; vacío -> null (varios socios pueden no tener email)
    static String normalizarEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    // "  José   PÉREZ " -> "jose perez": sin mayúsculas, tildes ni espacios de más
    static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    // "+54 9 2345-68 8807" -> "5492345688807": sin espacios, guiones, "+", paréntesis ni puntos
    static String normalizarTelefono(String telefono) {
        return telefono == null ? "" : telefono.replaceAll("[\\s\\-+().]", "");
    }

    // Marca un socio como inactivo (baja lógica, no borrado físico)
    public void darDeBaja(Long id) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        socio.setActivo(false);
        socioRepository.save(socio);
    }

    // Reactiva un socio inactivo
    public void reactivar(Long id) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        socio.setActivo(true);
        socioRepository.save(socio);
    }

    // Búsqueda por texto en nombre o apellido
    public List<Socio> buscarPorTexto(String texto) {
        return socioRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(texto, texto);
    }

    // Guarda el objetivo elegido del socio.
    // Un codigo null/vacío deja el objetivo sin definir (se limpia el valor vigente).
    // Lanza IllegalArgumentException si el código no pertenece al catálogo.
    public void actualizarObjetivo(Long id, String codigo) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));

        String objetivo = (codigo == null || codigo.isBlank()) ? null : codigo.trim();

        if (objetivo != null && !Objetivo.esValido(objetivo)) {
            throw new IllegalArgumentException("El objetivo seleccionado no es válido");
        }

        socio.setObjetivo(objetivo);
        socioRepository.save(socio);
    }
}