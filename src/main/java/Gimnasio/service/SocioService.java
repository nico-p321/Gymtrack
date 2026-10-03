package Gimnasio.service;

import Gimnasio.model.Socio;
import Gimnasio.repository.SocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
        Optional<Socio> existente = socioRepository.findByEmail(socio.getEmail());

        // Si existe un socio con ese email Y no es el mismo que estoy editando -> error
        if (existente.isPresent() && !existente.get().getIdSocio().equals(socio.getIdSocio())) {
            return "Ese email ya pertenece a otro socio";
        }

        socioRepository.save(socio);
        return null; // null = sin errores
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
}