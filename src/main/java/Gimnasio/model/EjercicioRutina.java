package Gimnasio.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Ejercicio planificado por el entrenador para un día de la rutina semanal del socio.
 * A diferencia de Entreno, no tiene fecha: es la plantilla que se repite cada semana.
 */
@Entity
@Table(name = "ejercicios_rutina")
public class EjercicioRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEjercicioRutina;

    @ManyToOne
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiaRutina dia;

    @Column(nullable = false, length = 100)
    private String ejercicio;

    @Column(nullable = false)
    private Integer series;

    @Column(nullable = false)
    private Integer repeticiones;

    @Column
    private BigDecimal peso; // en kg, opcional

    public Long getIdEjercicioRutina() { return idEjercicioRutina; }
    public void setIdEjercicioRutina(Long idEjercicioRutina) { this.idEjercicioRutina = idEjercicioRutina; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public DiaRutina getDia() { return dia; }
    public void setDia(DiaRutina dia) { this.dia = dia; }

    public String getEjercicio() { return ejercicio; }
    public void setEjercicio(String ejercicio) { this.ejercicio = ejercicio; }

    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }

    public Integer getRepeticiones() { return repeticiones; }
    public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }
}
