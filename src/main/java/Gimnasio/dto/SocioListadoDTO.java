package Gimnasio.dto;

import Gimnasio.model.Socio;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;

public class SocioListadoDTO {

    private Long idSocio;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private LocalDate fechaAlta;
    private boolean activo;
    private String estadoCuota;
    private Long diasVencido;
    private boolean critico;

    public SocioListadoDTO(@NonNull Socio socio, String estadoCuota, Long diasVencido, boolean critico) {
        this.idSocio = socio.getIdSocio();
        this.nombre = socio.getNombre();
        this.apellido = socio.getApellido();
        this.telefono = socio.getTelefono();
        this.email = socio.getEmail();
        this.fechaAlta = socio.getFechaAlta();
        this.activo = socio.isActivo();
        this.estadoCuota = estadoCuota;
        this.diasVencido = diasVencido;
        this.critico = critico;
    }

    public Long getIdSocio() { return idSocio; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public boolean isActivo() { return activo; }
    public String getEstadoCuota() { return estadoCuota; }
    public Long getDiasVencido() { return diasVencido; }
    public boolean isCritico() { return critico; }
}