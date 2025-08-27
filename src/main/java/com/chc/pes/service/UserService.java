package com.chc.pes.service;

import com.chc.pes.persistence.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.chc.pes.exception.UserNotFoundException;
import com.chc.pes.persistence.entity.UserEntity;
import com.chc.pes.persistence.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User Id not found: " + id));
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Username not found: " + username));
    }

    public List<UserEntity> getAllUsers() {
        return (List<UserEntity>) userRepository.findAll();
    }

    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User Id not found: " + id);
        }
        userRepository.deleteById(id);
    }

    public void updateUser(UserEntity user) {
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException("User Id not found: " + user.getId());
        }
        userRepository.save(user);
    }

    @Transactional
    public UserEntity insertOrUpdateUser(String username, String role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es requerido.");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("El rol es requerido.");
        }

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El rol '" + role + "' no es válido.");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    // El usuario existe, verificar si el rol ha cambiado
                    if (!user.getRole().equals(userRole)) {
                        user.setRole(userRole);
                        return userRepository.save(user); // Guardar solo si el rol cambió
                    }
                    return user; // El rol es el mismo, no hacer nada
                })
                .orElseGet(() -> {
                    // El usuario no existe, crear uno nuevo
                    UserEntity newUser = new UserEntity();
                    newUser.setUsername(username);
                    newUser.setRole(userRole);
                    newUser.setLocked(false);
                    return userRepository.save(newUser);
                });
    }
}
