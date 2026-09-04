package Gimnasio.service;

import Gimnasio.model.Socio;
import Gimnasio.repository.SocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    public List<Socio> listarActivos() {
        return socioRepository.findByActivoTrue();
    }

    public List<Socio> listarTodos() {
        return socioRepository.findAll();
    }

    public Optional<Socio> buscarPorId(Long id) {
        return socioRepository.findById(id);
    }

    public Socio guardar(Socio socio) {
        return socioRepository.save(socio);
    }

    // Valida email duplicado antes de guardar (alta o edición)
    public String guardarConValidacion(Socio socio) {
        Optional<Socio> existente = socioRepository.findByEmail(socio.getEmail());

        // Si existe un socio con ese email Y no es el mismo que estoy editando -> error
        if (existente.isPresent() && !existente.get().getIdSocio().equals(socio.getIdSocio())) {
            return "Ese email ya pertenece a otro socio";
        }

        socioRepository.save(socio);
        return null; // null = sin errores
    }

    public void darDeBaja(Long id) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        socio.setActivo(false);
        socioRepository.save(socio);
    }

    public void reactivar(Long id) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado"));
        socio.setActivo(true);
        socioRepository.save(socio);
    }

    public List<Socio> buscarPorTexto(String texto) {
        return socioRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(texto, texto);
    }
}