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

/**
 * Servicio que encapsula la lógica de negocio para la gestión de cuotas/pagos.
 * Maneja el registro de pagos, historial, cálculo de estado de cuenta y valor de cuota.
 */
@Service
public class CuotaService {

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private CuotaValorRepository cuotaValorRepository;

    // Registra un pago para un socio. Si no se pasa monto, usa el valor vigente actual.
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
    // La cuota vence a 1 mes de la fecha de pago
    public String calcularEstado(Long idSocio) {
        Optional<Cuota> ultima = ultimoPago(idSocio);

        if (ultima.isEmpty()) {
            return "SIN PAGOS REGISTRADOS";
        }

        LocalDate vencimiento = ultima.get().getFechaPago().plusMonths(1);
        return vencimiento.isBefore(LocalDate.now()) ? "VENCIDO" : "AL DÍA";
    }

    // Calcula la fecha de vencimiento sumando 1 mes a la fecha de pago
    public LocalDate calcularVencimiento(LocalDate fechaPago) {
        return fechaPago.plusMonths(1);
    }

    // Obtiene el valor de cuota vigente actual (el más reciente)
    public Optional<CuotaValor> obtenerValorVigente() {
        return cuotaValorRepository.findTopByOrderByVigenteDesdeDesc();
    }

    // Crea un nuevo registro de valor de cuota con fecha de vigencia actual
    public CuotaValor actualizarValorCuota(BigDecimal nuevoMonto) {
        CuotaValor nuevo = new CuotaValor();
        nuevo.setMonto(nuevoMonto);
        nuevo.setVigenteDesde(LocalDate.now());
        return cuotaValorRepository.save(nuevo);
    }
}