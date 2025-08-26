package com.chc.pes.service;

import org.springframework.stereotype.Service;

@Service // Marca esta clase como un bean de servicio de Spring
public class ExternalAuthService {

    /**
     * Valida las credenciales del usuario.
     * TODO: Implementar la lógica real de validación contra el servicio externo.
     *
     * @param username El nombre de usuario.
     * @param password La contraseña proporcionada.
     * @return true si las credenciales son válidas (actualmente siempre devuelve true), false en caso contrario.
     */
    public boolean validateCredentials(String username, String password) {
        // De momento, siempre devuelve verdadero para permitir el acceso
        // Aquí iría la lógica para llamar al servicio de autenticación externo real
        System.out.println("Validando credenciales externamente para: " + username + " (simulado, siempre OK)"); // Log de ejemplo
        return true;
    }
}