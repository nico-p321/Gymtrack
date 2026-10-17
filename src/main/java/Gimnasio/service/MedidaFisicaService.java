package Gimnasio.service;

import Gimnasio.model.MedidaFisica;
import Gimnasio.model.Socio;
import Gimnasio.repository.MedidaFisicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MedidaFisicaService {

    @Autowired
    private MedidaFisicaRepository medidaFisicaRepository;

    public MedidaFisica registrarMedida(Socio socio, LocalDate fecha, BigDecimal peso,
                                        BigDecimal altura, BigDecimal grasaCorporal) {

        if (peso == null || peso.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero");
        }
        if (altura == null || altura.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La altura debe ser mayor a cero");
        }

        MedidaFisica medida = new MedidaFisica();
        medida.setSocio(socio);
        medida.setFecha(fecha != null ? fecha : LocalDate.now());
        medida.setPeso(peso);
        medida.setAltura(altura);
        medida.setGrasaCorporal(grasaCorporal);

        return medidaFisicaRepository.save(medida);
    }

    public List<MedidaFisica> listarPorSocio(Long idSocio) {
        return medidaFisicaRepository.findBySocioIdSocioOrderByFechaDesc(idSocio);
    }
}