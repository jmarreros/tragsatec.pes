package com.chc.pes.service;

import com.chc.pes.dto.LdapAutentificacion;
import com.chc.pes.persistence.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LdapAutentificacionService {

    // *********************************************************************************
    public LdapAutentificacion autentificacion(String usuario, String password) {
        return new LdapAutentificacion(LdapAutentificacion.Estado.OK, "admin", UserRole.REGISTRADOR);
    }

}