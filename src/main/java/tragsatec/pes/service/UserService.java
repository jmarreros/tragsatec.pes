package tragsatec.pes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tragsatec.pes.exception.UserNotFoundException;
import tragsatec.pes.persistence.entity.UserEntity;
import tragsatec.pes.persistence.repository.UserRepository;

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
}
