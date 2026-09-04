package Gimnasio.service;

import Gimnasio.model.Cuota;
import Gimnasio.model.CuotaValor;
import Gimnasio.model.Socio;
import Gimnasio.repository.CuotaRepository;
import Gimnasio.repository.CuotaValorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CuotaService {

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private CuotaValorRepository cuotaValorRepository;

    // Registra un pago para un socio. Si no se pasa monto, usa el vigente.
    public Cuota registrarPago(Socio socio, LocalDate fechaPago, BigDecimal monto) {
        BigDecimal montoFinal = monto;

        if (montoFinal == null) {
            CuotaValor valorVigente = cuotaValorRepository.findTopByOrderByVigenteDesdeDesc()
                    .orElseThrow(() -> new RuntimeException("No hay un valor de cuota configurado todavía"));
            montoFinal = valorVigente.getMonto();
        }

        Cuota cuota = new Cuota();
        cuota.setSocio(socio);
        cuota.setFechaPago(fechaPago);
        cuota.setMonto(montoFinal);

        return cuotaRepository.save(cuota);
    }

    // Historial de pagos de un socio, más reciente primero
    public List<Cuota> historialDePagos(Long idSocio) {
        return cuotaRepository.findBySocioIdSocioOrderByFechaPagoDesc(idSocio);
    }

    // Última cuota pagada (para saber el estado actual de la cuenta)
    public Optional<Cuota> ultimoPago(Long idSocio) {
        return cuotaRepository.findTopBySocioIdSocioOrderByFechaPagoDesc(idSocio);
    }

    // Calcula si el socio está al día o vencido, en base a su último pago
    public String calcularEstado(Long idSocio) {
        Optional<Cuota> ultima = ultimoPago(idSocio);

        if (ultima.isEmpty()) {
            return "SIN PAGOS REGISTRADOS";
        }

        LocalDate vencimiento = ultima.get().getFechaPago().plusMonths(1);
        return vencimiento.isBefore(LocalDate.now()) ? "VENCIDO" : "AL DÍA";
    }

    public LocalDate calcularVencimiento(LocalDate fechaPago) {
        return fechaPago.plusMonths(1);
    }
}