package com.chc.pes.dto;

import com.chc.pes.persistence.enums.UserRole;
import lombok.Getter;

@Getter
public class LdapAutentificacion {
    public enum Estado {
        OK,
        ERROR,
        DENEGADO,
        NO_ENCONTRADO;
    }

    private Estado estado;
    private String usuario;
    private UserRole rol;

    public LdapAutentificacion(Estado estado, String usuario, UserRole rol) {
        this.estado = estado;
        this.usuario = usuario;
        this.rol = rol;
    }

    public LdapAutentificacion(Estado estado) {
        this.estado = estado;
    }

}