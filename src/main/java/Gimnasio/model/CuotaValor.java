package Gimnasio.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que almacena el valor histórico de la cuota mensual.
 * Permite mantener un historial de cambios de precio con su fecha de vigencia.
 * Mapea a la tabla "cuota_valor" en la base de datos.
 */
@Entity
@Table(name = "cuota_valor")
public class CuotaValor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate vigenteDesde;

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public void setVigenteDesde(LocalDate vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }
}