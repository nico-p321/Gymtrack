package Gimnasio.model;

import jakarta.persistence.*;

/**
 * Grupos musculares que el entrenador asignó a un día de la rutina semanal del socio.
 * Hay como máximo un registro por socio y día.
 */
@Entity
@Table(name = "rutina_dias",
        uniqueConstraints = @UniqueConstraint(columnNames = {"socio_id", "dia"}))
public class RutinaDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRutinaDia;

    @ManyToOne
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiaRutina dia;

    @Column(length = 200)
    private String grupos; // códigos de GrupoMuscular separados por coma, ej: "PECHO,HOMBRO"

    public Long getIdRutinaDia() { return idRutinaDia; }
    public void setIdRutinaDia(Long idRutinaDia) { this.idRutinaDia = idRutinaDia; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public DiaRutina getDia() { return dia; }
    public void setDia(DiaRutina dia) { this.dia = dia; }

    public String getGrupos() { return grupos; }
    public void setGrupos(String grupos) { this.grupos = grupos; }
}
