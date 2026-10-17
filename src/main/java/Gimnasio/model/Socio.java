package Gimnasio.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa a un socio (miembro) del gimnasio.
 * Mapea a la tabla "socios" en la base de datos.
 */
@Entity
@Table(name = "socios")
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSocio;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    @Column(unique = true, length = 100)
    private String email;

    private LocalDate fechaAlta = LocalDate.now();

    private boolean activo = true;

    // Objetivo del socio dentro del gimnasio (código de Gimnasio.model.Objetivo).
    // Puede ser null: significa que todavía no definió su objetivo.
    @Column(length = 30)
    private String objetivo;

    // Getters y setters

    public Long getIdSocio() {
        return idSocio;
    }

    public void setIdSocio(Long idSocio) {
        this.idSocio = idSocio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    // Etiqueta legible del objetivo para las vistas; "Sin definir" si no tiene ninguno
    @Transient
    public String getObjetivoEtiqueta() {
        return Objetivo.etiquetaDe(objetivo);
    }
}