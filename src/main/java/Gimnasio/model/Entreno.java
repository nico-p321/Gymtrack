package Gimnasio.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa un ejercicio realizado por un socio en una fecha determinada.
 * Mapea a la tabla "entrenos" en la base de datos.
 * Cada registro guarda un solo ejercicio, de modo que cargarlos el mismo día
 * produce varias filas con la misma fecha.
 */
@Entity
@Table(name = "entrenos")
public class Entreno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEntreno;

    @ManyToOne
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 100)
    private String ejercicio;

    @Column(nullable = false)
    private Integer series;

    @Column(nullable = false)
    private Integer repeticiones;

    @Column
    private BigDecimal peso; // en kg, opcional para ejercicios con peso corporal

    // Getters y setters

    public Long getIdEntreno() {
        return idEntreno;
    }

    public void setIdEntreno(Long idEntreno) {
        this.idEntreno = idEntreno;
    }

    public Socio getSocio() {
        return socio;
    }

    public void setSocio(Socio socio) {
        this.socio = socio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(String ejercicio) {
        this.ejercicio = ejercicio;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }
}