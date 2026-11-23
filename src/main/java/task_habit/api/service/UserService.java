package task_habit.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task_habit.api.dto.ChangePasswordRequest;
import task_habit.api.dto.RegisterRequest;
import task_habit.api.dto.UpdateUserRequest;
import task_habit.api.dto.UserDTO;
import task_habit.api.exception.BadRequestException;
import task_habit.api.exception.ResourceNotFoundException;
import task_habit.api.model.User;
import task_habit.api.repository.UserRepository;
import task_habit.api.specifications.UserSpecifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------------- aktuálny používateľ

    /** Prihlásený používateľ podľa SecurityContextu (nastavuje ho JwtRequestFilter). */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails details)) {
            throw new ResourceNotFoundException("Prihlásený používateľ neexistuje");
        }
        return this.userRepository.findByEmail(details.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Prihlásený používateľ neexistuje"));
    }

    public Long getCurrentUserId() {
        return this.getCurrentUser().getId();
    }

    // ---------------------------------------------------------------- registrácia / prihlásenie

    @Transactional
    public User register(RegisterRequest request) {
        String email = request.getEmail().trim();
        String username = request.getUsername().trim();
        if (this.userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Používateľ s týmto emailom už existuje");
        }
        if (this.userRepository.existsByUsername(username)) {
            throw new BadRequestException("Toto používateľské meno je už obsadené");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        return this.userRepository.save(user);
    }

    /** Vráti používateľa, ak sedí email aj heslo. */
    public Optional<User> authenticate(String email, String password) {
        return this.userRepository.findByEmail(email.trim())
                .filter(user -> this.passwordEncoder.matches(password, user.getPassword()));
    }

    // ---------------------------------------------------------------- profil

    @Transactional
    public User updateCurrentUser(UpdateUserRequest request) {
        User user = this.getCurrentUser();
        String newEmail = request.getEmail().trim();
        if (!newEmail.equals(user.getEmail()) && this.userRepository.findByEmail(newEmail).isPresent()) {
            throw new BadRequestException("Tento email už používa iný účet");
        }
        String newUsername = request.getUsername().trim();
        if (!newUsername.equals(user.getUsername()) && this.userRepository.existsByUsername(newUsername)) {
            throw new BadRequestException("Toto používateľské meno je už obsadené");
        }
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        return this.userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = this.getCurrentUser();
        if (!this.passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Aktuálne heslo nie je správne");
        }
        user.setPassword(this.passwordEncoder.encode(request.getNewPassword()));
        this.userRepository.save(user);
    }

    @Transactional
    public void deleteCurrentUser() {
        this.userRepository.delete(this.getCurrentUser());
    }

    // ---------------------------------------------------------------- ostatné

    public List<UserDTO> getAllUsers() {
        return this.userRepository.findAll().stream().map(UserDTO::fromEntity).toList();
    }

    public Page<UserDTO> getAllUsersPageable(Pageable pageable) {
        return this.userRepository.findAll(pageable).map(UserDTO::fromEntity);
    }

    public Optional<User> getUserById(Long id) {
        return this.userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        Specification<User> spec = UserSpecifications.hasEmail(email);
        return this.userRepository.findOne(spec);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
