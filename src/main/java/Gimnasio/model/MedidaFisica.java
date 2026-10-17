package Gimnasio.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "medidas_fisicas")
public class MedidaFisica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedida;

    @ManyToOne
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private BigDecimal peso;          // en kg

    @Column(nullable = false)
    private BigDecimal altura;        // en cm

    private BigDecimal grasaCorporal; // en %, opcional

    public Long getIdMedida() { return idMedida; }
    public void setIdMedida(Long idMedida) { this.idMedida = idMedida; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getAltura() { return altura; }
    public void setAltura(BigDecimal altura) { this.altura = altura; }

    public BigDecimal getGrasaCorporal() { return grasaCorporal; }
    public void setGrasaCorporal(BigDecimal grasaCorporal) { this.grasaCorporal = grasaCorporal; }
}