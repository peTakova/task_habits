package task_habit.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import task_habit.api.model.User;
import task_habit.api.repository.UserRepository;
import task_habit.api.specifications.UserSpecifications;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        if (this.userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Používateľ s týmto emailom už existuje.");
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return this.userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        Specification<User> spec = UserSpecifications.hasEmail(email);
        return this.userRepository.findOne(spec);
    }

    public User saveUser(User user) {
        return this.userRepository.save(user);
    }

    public User createUser(User user) {
        return this.userRepository.save(user);
    }

    public boolean deleteUserById(Long id) {
        if (this.userRepository.existsById(id)) {
            this.userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
