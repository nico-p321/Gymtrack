package Gimnasio.dto;

import Gimnasio.model.Socio;

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

    public SocioListadoDTO(Socio socio, String estadoCuota) {
        this.idSocio = socio.getIdSocio();
        this.nombre = socio.getNombre();
        this.apellido = socio.getApellido();
        this.telefono = socio.getTelefono();
        this.email = socio.getEmail();
        this.fechaAlta = socio.getFechaAlta();
        this.activo = socio.isActivo();
        this.estadoCuota = estadoCuota;
    }

    public Long getIdSocio() { return idSocio; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public boolean isActivo() { return activo; }
    public String getEstadoCuota() { return estadoCuota; }
}